package com.movieflux.domain.movies

import com.movieflux.core.common.ResultOf

interface MoviesRepository {
    suspend fun getPopularMovies(page: Int): ResultOf<PagedResult<Movie>>

    suspend fun searchMovies(
        query: String,
        page: Int,
    ): ResultOf<PagedResult<Movie>>

    suspend fun getMovieDetails(movieId: Int): ResultOf<Movie>
}
