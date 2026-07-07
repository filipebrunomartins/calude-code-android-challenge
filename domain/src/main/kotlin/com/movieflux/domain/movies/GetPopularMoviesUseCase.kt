package com.movieflux.domain.movies

import com.movieflux.core.common.ResultOf
import javax.inject.Inject

class GetPopularMoviesUseCase
    @Inject
    constructor(
        private val moviesRepository: MoviesRepository,
    ) {
        suspend operator fun invoke(page: Int): ResultOf<PagedResult<Movie>> = moviesRepository.getPopularMovies(page)
    }
