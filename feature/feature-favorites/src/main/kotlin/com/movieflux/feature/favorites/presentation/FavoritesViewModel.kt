package com.movieflux.feature.favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieflux.domain.favorites.ObserveFavoritesUseCase
import com.movieflux.domain.favorites.ToggleFavoriteUseCase
import com.movieflux.domain.movies.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = true,
) {
    val isEmpty: Boolean get() = !isLoading && movies.isEmpty()
}

@HiltViewModel
class FavoritesViewModel
    @Inject
    constructor(
        observeFavoritesUseCase: ObserveFavoritesUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    ) : ViewModel() {
        val uiState: StateFlow<FavoritesUiState> =
            observeFavoritesUseCase()
                .map { movies -> FavoritesUiState(movies = movies, isLoading = false) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState(isLoading = true))

        fun onFavoriteClick(movie: Movie) {
            viewModelScope.launch { toggleFavoriteUseCase(movie) }
        }
    }
