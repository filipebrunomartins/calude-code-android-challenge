package com.movieflux.data.favorites

import com.movieflux.data.favorites.local.FavoriteMovieDao
import com.movieflux.data.favorites.local.FavoriteMovieEntity
import com.movieflux.domain.favorites.FavoritesRepository
import com.movieflux.domain.movies.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl
    @Inject
    constructor(
        private val favoriteMovieDao: FavoriteMovieDao,
    ) : FavoritesRepository {
        override fun observeFavoriteIds(): Flow<Set<Int>> = favoriteMovieDao.observeFavoriteIds().map { it.toSet() }

        override fun observeIsFavorite(movieId: Int): Flow<Boolean> = favoriteMovieDao.observeIsFavorite(movieId)

        override fun observeFavorites(): Flow<List<Movie>> =
            favoriteMovieDao.observeAll().map { entities -> entities.map { it.toDomain() } }

        override suspend fun toggleFavorite(movie: Movie) {
            val existing = favoriteMovieDao.findById(movie.id)
            if (existing != null) {
                favoriteMovieDao.delete(existing)
            } else {
                favoriteMovieDao.insert(movie.toEntity())
            }
        }
    }

private fun FavoriteMovieEntity.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage,
        genres = genres,
        isFavorite = true,
    )

private fun Movie.toEntity(): FavoriteMovieEntity =
    FavoriteMovieEntity(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage,
        genres = genres,
    )
