package com.movieflux.data.di

import android.content.Context
import androidx.room.Room
import com.movieflux.data.favorites.local.FavoriteMovieDao
import com.movieflux.data.local.MovieFluxDatabase
import com.movieflux.data.movies.local.GenreDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "movieflux.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMovieFluxDatabase(@ApplicationContext context: Context): MovieFluxDatabase =
        Room.databaseBuilder(context, MovieFluxDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideGenreDao(database: MovieFluxDatabase): GenreDao = database.genreDao()

    @Provides
    fun provideFavoriteMovieDao(database: MovieFluxDatabase): FavoriteMovieDao = database.favoriteMovieDao()
}
