package com.movieflux.data.movies.remote

import com.movieflux.data.movies.remote.dto.GenreListResponseDto
import com.movieflux.data.movies.remote.dto.MovieDetailDto
import com.movieflux.data.movies.remote.dto.MoviePageResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("page") page: Int): MoviePageResponseDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int,
    ): MoviePageResponseDto

    @GET("genre/movie/list")
    suspend fun getGenres(): GenreListResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(@Path("movie_id") movieId: Int): MovieDetailDto
}
