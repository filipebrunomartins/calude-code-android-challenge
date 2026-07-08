package com.movieflux.data.movies

import com.movieflux.core.common.ResultOf
import com.movieflux.core.common.map
import com.movieflux.core.network.safeApiCall
import com.movieflux.data.movies.remote.TmdbApiService
import com.movieflux.data.movies.remote.toDomain
import com.movieflux.domain.movies.GenreRepository
import com.movieflux.domain.movies.Movie
import com.movieflux.domain.movies.MoviesRepository
import com.movieflux.domain.movies.PagedResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepositoryImpl
    @Inject
    constructor(
        private val tmdbApiService: TmdbApiService,
        private val genreRepository: GenreRepository,
    ) : MoviesRepository {
        override suspend fun getPopularMovies(page: Int): ResultOf<PagedResult<Movie>> {
            genreRepository.refreshGenresIfNeeded()
            val genreMap = genreRepository.observeGenreMap().first()

            return safeApiCall { tmdbApiService.getPopularMovies(page) }.map { response ->
                PagedResult(
                    items = response.results.map { it.toDomain(genreMap) },
                    page = response.page,
                    totalPages = response.totalPages,
                )
            }
        }

        override suspend fun searchMovies(
            query: String,
            page: Int,
        ): ResultOf<PagedResult<Movie>> {
            genreRepository.refreshGenresIfNeeded()
            val genreMap = genreRepository.observeGenreMap().first()

            return safeApiCall { tmdbApiService.searchMovies(query, page) }.map { response ->
                PagedResult(
                    items = response.results.map { it.toDomain(genreMap) },
                    page = response.page,
                    totalPages = response.totalPages,
                )
            }
        }

        override suspend fun getMovieDetails(movieId: Int): ResultOf<Movie> = safeApiCall { tmdbApiService.getMovieDetails(movieId) }.map { it.toDomain() }
    }
