package com.movieflux.domain.movies

data class PagedResult<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
) {
    val hasMorePages: Boolean get() = page < totalPages
}
