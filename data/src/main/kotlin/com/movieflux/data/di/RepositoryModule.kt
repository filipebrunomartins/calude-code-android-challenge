package com.movieflux.data.di

import com.movieflux.core.security.EncryptedPreferencesStorage
import com.movieflux.core.security.SecureStorage
import com.movieflux.data.auth.AuthRepositoryImpl
import com.movieflux.data.favorites.FavoritesRepositoryImpl
import com.movieflux.data.movies.GenreRepositoryImpl
import com.movieflux.data.movies.MoviesRepositoryImpl
import com.movieflux.domain.auth.AuthRepository
import com.movieflux.domain.favorites.FavoritesRepository
import com.movieflux.domain.movies.GenreRepository
import com.movieflux.domain.movies.MoviesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSecureStorage(impl: EncryptedPreferencesStorage): SecureStorage

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMoviesRepository(impl: MoviesRepositoryImpl): MoviesRepository

    @Binds
    @Singleton
    abstract fun bindGenreRepository(impl: GenreRepositoryImpl): GenreRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository
}
