package com.movieflux.domain.auth

import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IsBiometricEnabledUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean = authRepository.observeSession().first().biometricEnabled
}
