package com.movieflux.data.favorites

import app.cash.turbine.test
import com.movieflux.data.favorites.local.FavoriteMovieDao
import com.movieflux.data.favorites.local.FavoriteMovieEntity
import com.movieflux.domain.movies.Movie
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoritesRepositoryImplTest {
    private val dao: FavoriteMovieDao = mockk(relaxUnitFun = true)

    private val repository = FavoritesRepositoryImpl(dao)

    private val movie =
        Movie(
            id = 42,
            title = "Duna",
            overview = "Um jovem herdeiro em um planeta desértico.",
            posterPath = "/duna-poster.jpg",
            backdropPath = "/duna-backdrop.jpg",
            voteAverage = 8.1,
            genres = listOf("Ficção científica", "Aventura"),
        )

    private val entity =
        FavoriteMovieEntity(
            id = 42,
            title = "Duna",
            overview = "Um jovem herdeiro em um planeta desértico.",
            posterPath = "/duna-poster.jpg",
            backdropPath = "/duna-backdrop.jpg",
            voteAverage = 8.1,
            genres = listOf("Ficção científica", "Aventura"),
        )

    @Test
    fun `given ids repetidos emitidos pelo dao, when observar favoriteIds, then mapeia para set sem duplicatas`() =
        runTest {
            // given
            every { dao.observeFavoriteIds() } returns flowOf(listOf(1, 2, 2, 3))

            // when & then
            repository.observeFavoriteIds().test {
                assertEquals(setOf(1, 2, 3), awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `given movieId, when observar isFavorite, then delega direto para o dao`() =
        runTest {
            // given
            every { dao.observeIsFavorite(42) } returns flowOf(true)

            // when & then
            repository.observeIsFavorite(42).test {
                assertTrue(awaitItem())
                awaitComplete()
            }

            coVerify { dao.observeIsFavorite(42) }
        }

    @Test
    fun `given entities favoritadas no dao, when observar favoritos, then mapeia para Movie com isFavorite true`() =
        runTest {
            // given
            every { dao.observeAll() } returns flowOf(listOf(entity))

            // when & then
            repository.observeFavorites().test {
                val movies = awaitItem()
                assertEquals(1, movies.size)
                assertEquals(movie.copy(isFavorite = true), movies.first())
                awaitComplete()
            }
        }

    @Test
    fun `given filme já favoritado, when toggleFavorite, then remove do dao e não insere`() =
        runTest {
            // given
            coEvery { dao.findById(movie.id) } returns entity

            // when
            repository.toggleFavorite(movie)

            // then
            coVerify { dao.delete(entity) }
            coVerify(exactly = 0) { dao.insert(any()) }
        }

    @Test
    fun `given filme não favoritado, when toggleFavorite, then insere no dao preservando os campos e não remove`() =
        runTest {
            // given
            coEvery { dao.findById(movie.id) } returns null

            // when
            repository.toggleFavorite(movie)

            // then
            coVerify { dao.insert(entity) }
            coVerify(exactly = 0) { dao.delete(any()) }
        }
}
