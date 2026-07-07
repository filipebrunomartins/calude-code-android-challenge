package com.movieflux.domain.favorites

import com.movieflux.domain.movies.Movie
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoritesUseCase
    @Inject
    constructor(
        private val favoritesRepository: FavoritesRepository,
    ) {
        operator fun invoke(): Flow<List<Movie>> = favoritesRepository.observeFavorites()
    }
