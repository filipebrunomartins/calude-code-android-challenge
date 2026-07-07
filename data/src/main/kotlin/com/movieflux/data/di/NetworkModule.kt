package com.movieflux.data.di

import com.movieflux.core.network.ApiKeyInterceptor
import com.movieflux.core.network.BuildConfig
import com.movieflux.core.network.NetworkFactory
import com.movieflux.data.movies.remote.TmdbApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(apiKeyInterceptor: ApiKeyInterceptor): OkHttpClient =
        NetworkFactory.createOkHttpClient(apiKeyInterceptor)

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        NetworkFactory.createRetrofit(BuildConfig.TMDB_BASE_URL, okHttpClient)

    @Provides
    @Singleton
    fun provideTmdbApiService(retrofit: Retrofit): TmdbApiService =
        retrofit.create(TmdbApiService::class.java)
}
