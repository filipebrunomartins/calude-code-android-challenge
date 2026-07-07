package com.movieflux.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieflux.core.common.Failure
import com.movieflux.core.common.ResultOf
import com.movieflux.domain.favorites.ObserveFavoriteIdsUseCase
import com.movieflux.domain.favorites.ToggleFavoriteUseCase
import com.movieflux.domain.movies.GetPopularMoviesUseCase
import com.movieflux.domain.movies.Movie
import com.movieflux.domain.movies.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: Failure? = null,
    val searchQuery: String = "",
    val currentPage: Int = 0,
    val totalPages: Int = 1,
)

private const val SEARCH_DEBOUNCE_MS = 400L

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val searchMoviesUseCase: SearchMoviesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = combine(
        _uiState,
        observeFavoriteIdsUseCase(),
    ) { state, favoriteIds ->
        state.copy(movies = state.movies.map { it.copy(isFavorite = it.id in favoriteIds) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))

    private var searchJob: Job? = null

    init {
        loadNextPage()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(movies = emptyList(), currentPage = 0, totalPages = 1, errorMessage = null) }
            loadNextPage()
        }
    }

    fun onFavoriteClick(movie: Movie) {
        viewModelScope.launch { toggleFavoriteUseCase(movie) }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore) return
        if (state.currentPage > 0 && state.currentPage >= state.totalPages) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = state.currentPage == 0, isLoadingMore = state.currentPage > 0)
            }

            val nextPage = state.currentPage + 1
            val result = if (state.searchQuery.isBlank()) {
                getPopularMoviesUseCase(nextPage)
            } else {
                searchMoviesUseCase(state.searchQuery, nextPage)
            }

            when (result) {
                is ResultOf.Success -> _uiState.update {
                    it.copy(
                        movies = it.movies + result.data.items,
                        currentPage = result.data.page,
                        totalPages = result.data.totalPages,
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = null,
                    )
                }
                is ResultOf.Error -> _uiState.update {
                    it.copy(isLoading = false, isLoadingMore = false, errorMessage = result.failure)
                }
            }
        }
    }
}
