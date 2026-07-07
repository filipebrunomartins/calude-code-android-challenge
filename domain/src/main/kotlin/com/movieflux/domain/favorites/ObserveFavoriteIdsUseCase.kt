package com.movieflux.domain.favorites

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoriteIdsUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(): Flow<Set<Int>> = favoritesRepository.observeFavoriteIds()
}
