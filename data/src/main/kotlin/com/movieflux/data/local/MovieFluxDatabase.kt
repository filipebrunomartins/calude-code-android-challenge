package com.movieflux.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.movieflux.data.favorites.local.FavoriteMovieDao
import com.movieflux.data.favorites.local.FavoriteMovieEntity
import com.movieflux.data.movies.local.GenreDao
import com.movieflux.data.movies.local.GenreEntity

@Database(
    entities = [GenreEntity::class, FavoriteMovieEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MovieFluxDatabase : RoomDatabase() {
    abstract fun genreDao(): GenreDao

    abstract fun favoriteMovieDao(): FavoriteMovieDao
}
