package com.movieflux.feature.details.presentation

import com.movieflux.domain.favorites.FavoritesRepository
import com.movieflux.domain.movies.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Fake de [FavoritesRepository] com [MutableStateFlow] interno, usado para testar a sincronização
 * reativa de favoritos (ex. [DetailsViewModel]) sem mockar o [Flow] diretamente — ver skill
 * `android-testing`. Mesmo padrão do `FakeFavoritesRepository` de `feature-home`, replicado aqui
 * pois módulos de feature não podem importar um do outro.
 */
class FakeFavoritesRepository : FavoritesRepository {
    private val favoriteIds = MutableStateFlow<Set<Int>>(emptySet())

    override fun observeFavoriteIds(): Flow<Set<Int>> = favoriteIds.asStateFlow()

    override fun observeIsFavorite(movieId: Int): Flow<Boolean> = favoriteIds.map { movieId in it }

    override fun observeFavorites(): Flow<List<Movie>> = throw NotImplementedError("Não usado nos testes de DetailsViewModel")

    override suspend fun toggleFavorite(movie: Movie) {
        favoriteIds.update { ids -> if (movie.id in ids) ids - movie.id else ids + movie.id }
    }

    fun emitFavoriteIds(ids: Set<Int>) {
        favoriteIds.value = ids
    }
}
