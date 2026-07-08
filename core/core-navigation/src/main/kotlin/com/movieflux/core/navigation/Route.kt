package com.movieflux.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Login : Route

    @Serializable
    data object BiometricGate : Route

    @Serializable
    data object Home : Route

    @Serializable
    data class Details(
        val movieId: Int,
    ) : Route

    @Serializable
    data object Favorites : Route
}
