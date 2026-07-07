package com.movieflux.feature.favorites.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movieflux.core.ui.components.EmptyState
import com.movieflux.core.ui.components.LoadingState
import com.movieflux.core.ui.components.MovieCard

private const val TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w342"

@Composable
fun FavoritesScreen(
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> LoadingState(modifier)
        uiState.isEmpty -> EmptyState(message = "Você ainda não favoritou nenhum filme", modifier = modifier)
        else ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = modifier.fillMaxSize(),
            ) {
                items(uiState.movies, key = { it.id }) { movie ->
                    MovieCard(
                        title = movie.title,
                        posterUrl = movie.posterPath?.let { "$TMDB_POSTER_BASE_URL$it" },
                        voteAverage = movie.voteAverage,
                        isFavorite = movie.isFavorite,
                        onClick = { onMovieClick(movie.id) },
                        onFavoriteClick = { viewModel.onFavoriteClick(movie) },
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
    }
}
