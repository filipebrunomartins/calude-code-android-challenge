package com.movieflux.feature.home.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movieflux.core.ui.components.EmptyState
import com.movieflux.core.ui.components.ErrorState
import com.movieflux.core.ui.components.LoadingState
import com.movieflux.core.ui.components.MovieCard
import com.movieflux.domain.movies.Movie

private const val TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w342"
private const val LOAD_MORE_THRESHOLD = 4

@Composable
fun HomeScreen(
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible =
                gridState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: 0
            val totalItems = gridState.layoutInfo.totalItemsCount
            totalItems > 0 && lastVisible >= totalItems - LOAD_MORE_THRESHOLD
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            label = { Text("Buscar filmes") },
            singleLine = true,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )

        when {
            uiState.isLoading && uiState.movies.isEmpty() -> LoadingState()
            uiState.errorMessage != null && uiState.movies.isEmpty() ->
                ErrorState(
                    failure = uiState.errorMessage!!,
                    onRetry = viewModel::loadNextPage,
                )
            uiState.movies.isEmpty() -> EmptyState(message = "Nenhum filme encontrado")
            else ->
                MovieGrid(
                    uiState = uiState,
                    gridState = gridState,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = viewModel::onFavoriteClick,
                )
        }
    }
}

@Composable
private fun MovieGrid(
    uiState: HomeUiState,
    gridState: LazyGridState,
    onMovieClick: (Int) -> Unit,
    onFavoriteClick: (Movie) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(uiState.movies, key = { it.id }) { movie ->
            MovieCard(
                title = movie.title,
                posterUrl = movie.posterPath?.let { "$TMDB_POSTER_BASE_URL$it" },
                voteAverage = movie.voteAverage,
                isFavorite = movie.isFavorite,
                onClick = { onMovieClick(movie.id) },
                onFavoriteClick = { onFavoriteClick(movie) },
                modifier = Modifier.padding(8.dp),
            )
        }
        if (uiState.isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
