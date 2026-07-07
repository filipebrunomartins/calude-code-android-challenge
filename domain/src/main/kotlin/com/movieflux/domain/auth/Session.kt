package com.movieflux.domain.auth

data class Session(
    val isLoggedIn: Boolean,
    val biometricEnabled: Boolean,
)
