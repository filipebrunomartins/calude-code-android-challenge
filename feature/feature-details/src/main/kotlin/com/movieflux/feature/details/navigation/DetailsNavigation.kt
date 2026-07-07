package com.movieflux.feature.details.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.movieflux.core.navigation.Route
import com.movieflux.feature.details.presentation.DetailsScreen

fun NavGraphBuilder.detailsGraph() {
    composable<Route.Details> {
        DetailsScreen()
    }
}
