package com.movieflux.domain.movies

import com.movieflux.core.common.ResultOf
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val moviesRepository: MoviesRepository,
) {
    suspend operator fun invoke(query: String, page: Int): ResultOf<PagedResult<Movie>> =
        moviesRepository.searchMovies(query, page)
}
