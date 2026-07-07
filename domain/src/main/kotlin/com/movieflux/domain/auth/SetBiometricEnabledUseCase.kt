package com.movieflux.domain.auth

import javax.inject.Inject

class SetBiometricEnabledUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = authRepository.setBiometricEnabled(enabled)
}
