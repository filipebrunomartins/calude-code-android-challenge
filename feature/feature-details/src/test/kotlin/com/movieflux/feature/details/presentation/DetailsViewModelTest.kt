package com.movieflux.feature.details.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.movieflux.core.common.Failure
import com.movieflux.core.common.ResultOf
import com.movieflux.domain.favorites.ObserveIsFavoriteUseCase
import com.movieflux.domain.favorites.ToggleFavoriteUseCase
import com.movieflux.domain.movies.GetMovieDetailsUseCase
import com.movieflux.domain.movies.Movie
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * `SavedStateHandle.toRoute<Route.Details>()` decodifica o argumento via
 * `androidx.navigation.serialization.RouteDecoder`, que internamente cria um `android.os.Bundle`
 * real (`BundleKt.bundleOf`) — em testes JVM puros isso lança
 * `RuntimeException: Method ... not mocked`. Rodar com Robolectric resolve, pois provê uma
 * implementação funcional de `android.os.Bundle`. `SavedStateHandle(mapOf("movieId" to id))`
 * funciona normalmente com Robolectric ativo (não foi necessário `.apply { set(...) }` nem
 * `navigation-testing`).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getMovieDetailsUseCase: GetMovieDetailsUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxUnitFun = true)
    private val fakeFavoritesRepository = FakeFavoritesRepository()
    private val observeIsFavoriteUseCase = ObserveIsFavoriteUseCase(fakeFavoritesRepository)

    private val testMovieId = 42

    private fun createViewModel(movieId: Int = testMovieId) =
        DetailsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("movieId" to movieId)),
            getMovieDetailsUseCase = getMovieDetailsUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            observeIsFavoriteUseCase = observeIsFavoriteUseCase,
        )

    private fun movie(id: Int) =
        Movie(
            id = id,
            title = "Filme $id",
            overview = "Sinopse do filme $id",
            posterPath = "/poster$id.jpg",
            backdropPath = "/backdrop$id.jpg",
            voteAverage = 8.0,
            genres = listOf("Drama"),
        )

    @Test
    fun `given movieId da rota, when o ViewModel é criado, then isLoading vai de true para false e o filme é carregado`() =
        runTest {
            // given
            val expectedMovie = movie(testMovieId)
            val deferredResult = CompletableDeferred<ResultOf<Movie>>()
            coEvery { getMovieDetailsUseCase(testMovieId) } coAnswers { deferredResult.await() }
            val viewModel = createViewModel()
            runCurrent()

            viewModel.uiState.test {
                // then (estado inicial em carregamento, sem filme)
                val loading = awaitItem()
                assertTrue(loading.isLoading)
                assertNull(loading.movie)

                // when
                deferredResult.complete(ResultOf.Success(expectedMovie))
                advanceUntilIdle()

                // then
                val loaded = awaitItem()
                assertFalse(loaded.isLoading)
                assertEquals(expectedMovie, loaded.movie)
                assertNull(loaded.errorMessage)
            }
        }

    @Test
    fun `given use case de detalhes retorna erro, when o ViewModel é criado, then errorMessage é preenchido e o filme continua nulo`() =
        runTest {
            // given
            coEvery { getMovieDetailsUseCase(testMovieId) } returns ResultOf.Error(Failure.Http(404))
            val viewModel = createViewModel()
            advanceUntilIdle()

            // then
            viewModel.uiState.test {
                // descarta o valor placeholder do stateIn antes da primeira recombinação real
                awaitItem()
                val state = awaitItem()
                assertEquals(Failure.Http(404), state.errorMessage)
                assertNull(state.movie)
                assertFalse(state.isLoading)
            }
        }

    @Test
    fun `given filme favoritado no repositório, when observar uiState, then isFavorite reflete o Set emitido pelo Flow`() =
        runTest {
            // given
            coEvery { getMovieDetailsUseCase(testMovieId) } returns ResultOf.Success(movie(testMovieId))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiState.test {
                // descarta o valor placeholder do stateIn antes da primeira recombinação real
                awaitItem()
                val initial = awaitItem()
                assertFalse(initial.isFavorite)

                // when
                fakeFavoritesRepository.emitFavoriteIds(setOf(testMovieId))

                // then
                val updated = awaitItem()
                assertTrue(updated.isFavorite)
            }
        }

    @Test
    fun `given filme carregado, when onFavoriteClick, then toggleFavoriteUseCase é chamado com o filme`() =
        runTest {
            // given
            val expectedMovie = movie(testMovieId)
            coEvery { getMovieDetailsUseCase(testMovieId) } returns ResultOf.Success(expectedMovie)
            val viewModel = createViewModel()
            advanceUntilIdle()

            // when
            viewModel.onFavoriteClick()
            advanceUntilIdle()

            // then
            coVerify { toggleFavoriteUseCase(expectedMovie) }
        }

    @Test
    fun `given filme ainda não carregado, when onFavoriteClick, then toggleFavoriteUseCase não é chamado`() =
        runTest {
            // given
            val deferredResult = CompletableDeferred<ResultOf<Movie>>()
            coEvery { getMovieDetailsUseCase(testMovieId) } coAnswers { deferredResult.await() }
            val viewModel = createViewModel()
            runCurrent()

            // when (movie ainda é null pois o use case não terminou)
            viewModel.onFavoriteClick()
            advanceUntilIdle()

            // then
            coVerify(exactly = 0) { toggleFavoriteUseCase(any()) }
        }
}
