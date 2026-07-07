package com.movieflux.domain.favorites

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveIsFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(movieId: Int): Flow<Boolean> = favoritesRepository.observeIsFavorite(movieId)
}
