package com.movieflux.feature.favorites.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.movieflux.core.navigation.Route
import com.movieflux.feature.favorites.presentation.FavoritesScreen

fun NavGraphBuilder.favoritesGraph(onMovieClick: (Int) -> Unit) {
    composable<Route.Favorites> {
        FavoritesScreen(onMovieClick = onMovieClick)
    }
}
