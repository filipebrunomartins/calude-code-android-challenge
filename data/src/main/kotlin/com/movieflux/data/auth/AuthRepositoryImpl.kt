package com.movieflux.data.auth

import com.movieflux.core.common.Failure
import com.movieflux.core.common.ResultOf
import com.movieflux.core.security.SecureStorage
import com.movieflux.domain.auth.AuthRepository
import com.movieflux.domain.auth.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val KEY_IS_LOGGED_IN = "is_logged_in"
private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

private const val MOCK_USERNAME = "admin"
private const val MOCK_PASSWORD = "1234"

@Singleton
class AuthRepositoryImpl
    @Inject
    constructor(
        private val secureStorage: SecureStorage,
    ) : AuthRepository {
        private val sessionFlow: MutableStateFlow<Session> = MutableStateFlow(readSessionFromStorage())

        override suspend fun login(
            username: String,
            password: String,
        ): ResultOf<Unit> {
            if (username == MOCK_USERNAME && password == MOCK_PASSWORD) {
                secureStorage.putBoolean(KEY_IS_LOGGED_IN, true)
                sessionFlow.value = readSessionFromStorage()
                return ResultOf.Success(Unit)
            }
            return ResultOf.Error(Failure.Http(401))
        }

        override suspend fun logout() {
            secureStorage.putBoolean(KEY_IS_LOGGED_IN, false)
            sessionFlow.value = readSessionFromStorage()
        }

        override fun observeSession(): StateFlow<Session> = sessionFlow

        override suspend fun setBiometricEnabled(enabled: Boolean) {
            secureStorage.putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            sessionFlow.value = readSessionFromStorage()
        }

        private fun readSessionFromStorage(): Session =
            Session(
                isLoggedIn = secureStorage.getBoolean(KEY_IS_LOGGED_IN),
                biometricEnabled = secureStorage.getBoolean(KEY_BIOMETRIC_ENABLED),
            )
    }
