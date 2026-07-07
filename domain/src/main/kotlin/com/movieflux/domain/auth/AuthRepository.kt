package com.movieflux.domain.auth

import com.movieflux.core.common.ResultOf
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): ResultOf<Unit>

    suspend fun logout()

    fun observeSession(): Flow<Session>

    suspend fun setBiometricEnabled(enabled: Boolean)
}
