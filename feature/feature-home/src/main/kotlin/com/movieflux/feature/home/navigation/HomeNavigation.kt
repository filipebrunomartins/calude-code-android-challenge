package com.movieflux.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.movieflux.core.navigation.Route
import com.movieflux.feature.home.presentation.HomeScreen

fun NavGraphBuilder.homeGraph(onMovieClick: (Int) -> Unit) {
    composable<Route.Home> {
        HomeScreen(onMovieClick = onMovieClick)
    }
}
