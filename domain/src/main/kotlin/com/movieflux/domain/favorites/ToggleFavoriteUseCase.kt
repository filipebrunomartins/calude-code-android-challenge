package com.movieflux.domain.favorites

import com.movieflux.domain.movies.Movie
import javax.inject.Inject

class ToggleFavoriteUseCase
    @Inject
    constructor(
        private val favoritesRepository: FavoritesRepository,
    ) {
        suspend operator fun invoke(movie: Movie) = favoritesRepository.toggleFavorite(movie)
    }
