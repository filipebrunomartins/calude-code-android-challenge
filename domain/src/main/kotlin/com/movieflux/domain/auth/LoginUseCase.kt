package com.movieflux.domain.auth

import com.movieflux.core.common.ResultOf
import javax.inject.Inject

class LoginUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(
            username: String,
            password: String,
        ): ResultOf<Unit> = authRepository.login(username, password)
    }
