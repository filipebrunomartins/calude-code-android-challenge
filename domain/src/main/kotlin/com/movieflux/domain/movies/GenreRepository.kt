package com.movieflux.domain.movies

import com.movieflux.core.common.ResultOf
import kotlinx.coroutines.flow.Flow

interface GenreRepository {
    fun observeGenreMap(): Flow<Map<Int, String>>

    suspend fun refreshGenresIfNeeded(): ResultOf<Unit>
}
