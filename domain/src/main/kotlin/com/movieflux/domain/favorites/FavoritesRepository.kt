package com.movieflux.domain.favorites

import com.movieflux.domain.movies.Movie
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoriteIds(): Flow<Set<Int>>

    fun observeIsFavorite(movieId: Int): Flow<Boolean>

    fun observeFavorites(): Flow<List<Movie>>

    suspend fun toggleFavorite(movie: Movie)
}
