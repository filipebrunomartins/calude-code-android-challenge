package com.movieflux.feature.auth.presentation

import android.content.Context
import androidx.biometric.BiometricManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface BiometricCapabilityChecker {
    fun canAuthenticate(): Boolean
}

class AndroidBiometricCapabilityChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) : BiometricCapabilityChecker {

    override fun canAuthenticate(): Boolean {
        val result = BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }
}
