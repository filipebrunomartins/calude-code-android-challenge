package com.movieflux.domain.movies

import com.movieflux.core.common.ResultOf
import javax.inject.Inject

class GetMovieDetailsUseCase @Inject constructor(
    private val moviesRepository: MoviesRepository,
) {
    suspend operator fun invoke(movieId: Int): ResultOf<Movie> =
        moviesRepository.getMovieDetails(movieId)
}
