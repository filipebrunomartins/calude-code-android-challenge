package com.movieflux.data.movies.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenreDto(
    val id: Int,
    val name: String,
)

@Serializable
data class GenreListResponseDto(
    val genres: List<GenreDto> = emptyList(),
)
