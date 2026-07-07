package com.movieflux.domain.movies

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val genres: List<String>,
    val isFavorite: Boolean = false,
)
