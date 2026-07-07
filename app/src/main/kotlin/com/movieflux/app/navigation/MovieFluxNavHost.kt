package com.movieflux.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.movieflux.core.navigation.Route
import com.movieflux.core.ui.components.LoadingState
import com.movieflux.feature.auth.navigation.authGraph
import com.movieflux.feature.details.navigation.detailsGraph
import com.movieflux.feature.favorites.navigation.favoritesGraph
import com.movieflux.feature.home.navigation.homeGraph

@Composable
fun MovieFluxNavHost(rootViewModel: RootViewModel = hiltViewModel()) {
    val startDestination by rootViewModel.startDestination.collectAsStateWithLifecycle()
    val destination =
        startDestination ?: run {
            LoadingState()
            return
        }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (currentDestination.isInBottomBar()) {
                MovieFluxBottomBar(navController, currentDestination)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = destination,
            modifier = Modifier.padding(padding),
        ) {
            authGraph(navController)
            homeGraph(onMovieClick = { movieId -> navController.navigate(Route.Details(movieId)) })
            detailsGraph()
            favoritesGraph(onMovieClick = { movieId -> navController.navigate(Route.Details(movieId)) })
        }
    }
}

private fun NavDestination?.isInBottomBar(): Boolean = this?.hasRoute<Route.Home>() == true || this?.hasRoute<Route.Favorites>() == true

@Composable
private fun MovieFluxBottomBar(
    navController: NavController,
    currentDestination: NavDestination?,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentDestination?.hasRoute<Route.Home>() == true,
            onClick = {
                navController.navigate(Route.Home) {
                    launchSingleTop = true
                    popUpTo(Route.Home)
                }
            },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Início") },
            label = { Text("Início") },
        )
        NavigationBarItem(
            selected = currentDestination?.hasRoute<Route.Favorites>() == true,
            onClick = {
                navController.navigate(Route.Favorites) {
                    launchSingleTop = true
                    popUpTo(Route.Home)
                }
            },
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favoritos") },
            label = { Text("Favoritos") },
        )
    }
}
