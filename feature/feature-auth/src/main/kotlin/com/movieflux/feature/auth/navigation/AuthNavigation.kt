package com.movieflux.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.movieflux.core.navigation.Route
import com.movieflux.feature.auth.presentation.BiometricGateScreen
import com.movieflux.feature.auth.presentation.LoginScreen

fun NavGraphBuilder.authGraph(navController: NavController) {
    composable<Route.Login> {
        LoginScreen(
            onNavigateToHome = {
                navController.navigate(Route.Home) {
                    popUpTo(Route.Login) { inclusive = true }
                }
            },
        )
    }

    composable<Route.BiometricGate> {
        BiometricGateScreen(
            onAuthenticated = {
                navController.navigate(Route.Home) {
                    popUpTo(Route.BiometricGate) { inclusive = true }
                }
            },
            onFallbackToLogin = {
                navController.navigate(Route.Login) {
                    popUpTo(Route.BiometricGate) { inclusive = true }
                }
            },
        )
    }
}
