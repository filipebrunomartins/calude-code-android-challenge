package com.movieflux.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val PREFS_FILE_NAME = "movieflux_secure_prefs"

class EncryptedPreferencesStorage
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : SecureStorage {
        private val masterKey =
            MasterKey
                .Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        private val sharedPreferences =
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

        override fun putString(
            key: String,
            value: String,
        ) {
            sharedPreferences.edit().putString(key, value).apply()
        }

        override fun getString(key: String): String? = sharedPreferences.getString(key, null)

        override fun putBoolean(
            key: String,
            value: Boolean,
        ) {
            sharedPreferences.edit().putBoolean(key, value).apply()
        }

        override fun getBoolean(
            key: String,
            default: Boolean,
        ): Boolean = sharedPreferences.getBoolean(key, default)

        override fun remove(key: String) {
            sharedPreferences.edit().remove(key).apply()
        }
    }
