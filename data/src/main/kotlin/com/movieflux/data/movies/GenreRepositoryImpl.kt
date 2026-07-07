package com.movieflux.data.movies

import com.movieflux.core.common.ResultOf
import com.movieflux.core.network.safeApiCall
import com.movieflux.data.movies.local.GenreDao
import com.movieflux.data.movies.local.GenreEntity
import com.movieflux.data.movies.remote.TmdbApiService
import com.movieflux.domain.movies.GenreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenreRepositoryImpl
    @Inject
    constructor(
        private val tmdbApiService: TmdbApiService,
        private val genreDao: GenreDao,
    ) : GenreRepository {
        override fun observeGenreMap(): Flow<Map<Int, String>> =
            genreDao.observeAll().map { entities -> entities.associate { it.id to it.name } }

        override suspend fun refreshGenresIfNeeded(): ResultOf<Unit> {
            if (genreDao.count() > 0) return ResultOf.Success(Unit)

            return safeApiCall { tmdbApiService.getGenres() }.let { result ->
                when (result) {
                    is ResultOf.Success -> {
                        val entities = result.data.genres.map { GenreEntity(id = it.id, name = it.name) }
                        genreDao.insertAll(entities)
                        ResultOf.Success(Unit)
                    }
                    is ResultOf.Error -> result
                }
            }
        }
    }
