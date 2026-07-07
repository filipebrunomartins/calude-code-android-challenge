package com.movieflux.domain.auth

import com.movieflux.core.common.ResultOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fake de [AuthRepository] para testes de use cases/ViewModels que dependem de estado reativo
 * (ex. [AuthRepository.observeSession]). Usa um [MutableStateFlow] interno para reproduzir o
 * comportamento real do repositório em vez de mockar o Flow.
 */
class FakeAuthRepository(
    initialSession: Session = Session(isLoggedIn = false, biometricEnabled = false),
) : AuthRepository {
    var loginResult: ResultOf<Unit> = ResultOf.Success(Unit)
    val loginCalls = mutableListOf<Pair<String, String>>()
    var logoutCallCount = 0
    val biometricEnabledCalls = mutableListOf<Boolean>()

    private val sessionFlow = MutableStateFlow(initialSession)
    val session: StateFlow<Session> = sessionFlow

    override suspend fun login(
        username: String,
        password: String,
    ): ResultOf<Unit> {
        loginCalls += username to password
        return loginResult
    }

    override suspend fun logout() {
        logoutCallCount++
        sessionFlow.value = sessionFlow.value.copy(isLoggedIn = false)
    }

    override fun observeSession(): StateFlow<Session> = sessionFlow

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        biometricEnabledCalls += enabled
        sessionFlow.value = sessionFlow.value.copy(biometricEnabled = enabled)
    }
}
