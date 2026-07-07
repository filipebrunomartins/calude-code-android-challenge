package com.movieflux.data.movies.remote

import com.movieflux.data.movies.remote.dto.MovieDetailDto
import com.movieflux.data.movies.remote.dto.MovieDto
import com.movieflux.domain.movies.Movie

fun MovieDto.toDomain(genreMap: Map<Int, String>): Movie =
    Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage,
        genres = genreIds.mapNotNull { genreMap[it] },
    )

fun MovieDetailDto.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage,
        genres = genres.map { it.name },
    )
