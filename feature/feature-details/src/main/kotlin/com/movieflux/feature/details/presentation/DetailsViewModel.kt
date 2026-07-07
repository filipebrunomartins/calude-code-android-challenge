package com.movieflux.feature.details.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.movieflux.core.common.Failure
import com.movieflux.core.common.ResultOf
import com.movieflux.core.navigation.Route
import com.movieflux.domain.favorites.ObserveIsFavoriteUseCase
import com.movieflux.domain.favorites.ToggleFavoriteUseCase
import com.movieflux.domain.movies.GetMovieDetailsUseCase
import com.movieflux.domain.movies.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailsUiState(
    val movie: Movie? = null,
    val isLoading: Boolean = false,
    val errorMessage: Failure? = null,
    val isFavorite: Boolean = false,
)

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    observeIsFavoriteUseCase: ObserveIsFavoriteUseCase,
) : ViewModel() {

    private val movieId: Int = savedStateHandle.toRoute<Route.Details>().movieId

    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = true))

    val uiState: StateFlow<DetailsUiState> = combine(
        _uiState,
        observeIsFavoriteUseCase(movieId),
    ) { state, isFavorite ->
        state.copy(isFavorite = isFavorite)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailsUiState(isLoading = true))

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getMovieDetailsUseCase(movieId)) {
                is ResultOf.Success -> _uiState.update { it.copy(movie = result.data, isLoading = false) }
                is ResultOf.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.failure)
                }
            }
        }
    }

    fun onFavoriteClick() {
        val movie = _uiState.value.movie ?: return
        viewModelScope.launch { toggleFavoriteUseCase(movie) }
    }
}
