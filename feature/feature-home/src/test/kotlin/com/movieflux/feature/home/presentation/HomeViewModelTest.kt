package com.movieflux.feature.home.presentation

import app.cash.turbine.test
import com.movieflux.core.common.Failure
import com.movieflux.core.common.ResultOf
import com.movieflux.domain.favorites.ObserveFavoriteIdsUseCase
import com.movieflux.domain.favorites.ToggleFavoriteUseCase
import com.movieflux.domain.movies.GetPopularMoviesUseCase
import com.movieflux.domain.movies.Movie
import com.movieflux.domain.movies.PagedResult
import com.movieflux.domain.movies.SearchMoviesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPopularMoviesUseCase: GetPopularMoviesUseCase = mockk()
    private val searchMoviesUseCase: SearchMoviesUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxUnitFun = true)
    private val fakeFavoritesRepository = FakeFavoritesRepository()
    private val observeFavoriteIdsUseCase = ObserveFavoriteIdsUseCase(fakeFavoritesRepository)

    private fun createViewModel() =
        HomeViewModel(
            getPopularMoviesUseCase = getPopularMoviesUseCase,
            searchMoviesUseCase = searchMoviesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            observeFavoriteIdsUseCase = observeFavoriteIdsUseCase,
        )

    private fun movie(
        id: Int,
        title: String = "Filme $id",
    ) = Movie(
        id = id,
        title = title,
        overview = "Sinopse do filme $id",
        posterPath = "/poster$id.jpg",
        backdropPath = "/backdrop$id.jpg",
        voteAverage = 7.5,
        genres = listOf("Ação"),
    )

    private fun paged(
        items: List<Movie>,
        page: Int,
        totalPages: Int,
    ) = PagedResult(items = items, page = page, totalPages = totalPages)

    @Test
    fun `given página 1 em carregamento, when a busca conclui, then isLoading vai de true para false e os filmes são carregados`() =
        runTest {
            // given
            val moviesPage1 = listOf(movie(1), movie(2))
            val deferredResult = CompletableDeferred<ResultOf<PagedResult<Movie>>>()
            coEvery { getPopularMoviesUseCase(1) } coAnswers { deferredResult.await() }
            val viewModel = createViewModel()
            runCurrent()

            viewModel.uiState.test {
                // then (estado em andamento, ainda sem filmes)
                val loading = awaitItem()
                assertTrue(loading.isLoading)
                assertTrue(loading.movies.isEmpty())

                // when
                deferredResult.complete(ResultOf.Success(paged(moviesPage1, page = 1, totalPages = 3)))
                advanceUntilIdle()

                // then
                val loaded = awaitItem()
                assertFalse(loaded.isLoading)
                assertEquals(moviesPage1, loaded.movies)
                assertEquals(1, loaded.currentPage)
                assertEquals(3, loaded.totalPages)
            }
        }

    @Test
    fun `given página 1 já carregada com mais páginas disponíveis, when loadNextPage é chamado, then página 2 é carregada e acumulada à lista`() =
        runTest {
            // given
            val moviesPage1 = listOf(movie(1), movie(2))
            val moviesPage2 = listOf(movie(3), movie(4))
            coEvery { getPopularMoviesUseCase(1) } returns ResultOf.Success(paged(moviesPage1, page = 1, totalPages = 3))
            coEvery { getPopularMoviesUseCase(2) } returns ResultOf.Success(paged(moviesPage2, page = 2, totalPages = 3))
            val viewModel = createViewModel()
            advanceUntilIdle()

            // when
            viewModel.loadNextPage()
            advanceUntilIdle()

            // then
            viewModel.uiState.test {
                // o primeiro item é o valor placeholder do stateIn (WhileSubscribed só recomputa
                // a combinação após a primeira inscrição, então precisamos descartá-lo)
                awaitItem()
                val state = awaitItem()
                assertEquals(moviesPage1 + moviesPage2, state.movies)
                assertEquals(2, state.currentPage)
                assertEquals(3, state.totalPages)
                assertFalse(state.isLoadingMore)
            }
        }

    @Test
    fun `given página 1 ainda carregando, when loadNextPage é chamado novamente, then a segunda chamada é ignorada pelo guard`() =
        runTest {
            // given
            val deferredResult = CompletableDeferred<ResultOf<PagedResult<Movie>>>()
            coEvery { getPopularMoviesUseCase(1) } coAnswers { deferredResult.await() }
            coEvery { getPopularMoviesUseCase(2) } returns ResultOf.Success(paged(emptyList(), page = 2, totalPages = 3))
            val viewModel = createViewModel()
            runCurrent()

            // when (segunda chamada enquanto isLoading ainda é true)
            viewModel.loadNextPage()
            deferredResult.complete(ResultOf.Success(paged(listOf(movie(1)), page = 1, totalPages = 3)))
            advanceUntilIdle()

            // then
            coVerify(exactly = 1) { getPopularMoviesUseCase(1) }
            coVerify(exactly = 0) { getPopularMoviesUseCase(2) }
        }

    @Test
    fun `given currentPage igual a totalPages, when loadNextPage é chamado, then a paginação para e nenhuma nova chamada é feita`() =
        runTest {
            // given
            coEvery { getPopularMoviesUseCase(1) } returns ResultOf.Success(paged(listOf(movie(1)), page = 1, totalPages = 1))
            coEvery { getPopularMoviesUseCase(2) } returns ResultOf.Success(paged(emptyList(), page = 2, totalPages = 1))
            val viewModel = createViewModel()
            advanceUntilIdle()

            // when
            viewModel.loadNextPage()
            advanceUntilIdle()

            // then
            coVerify(exactly = 1) { getPopularMoviesUseCase(1) }
            coVerify(exactly = 0) { getPopularMoviesUseCase(2) }
        }

    @Test
    fun `given use case de filmes populares retorna erro, when carrega a página 1, then errorMessage é preenchido e currentPage não avança`() =
        runTest {
            // given
            coEvery { getPopularMoviesUseCase(1) } returns ResultOf.Error(Failure.Http(500))
            val viewModel = createViewModel()
            advanceUntilIdle()

            // then
            viewModel.uiState.test {
                // descarta o valor placeholder do stateIn antes da primeira recombinação real
                awaitItem()
                val state = awaitItem()
                assertEquals(Failure.Http(500), state.errorMessage)
                assertEquals(0, state.currentPage)
                assertTrue(state.movies.isEmpty())
                assertFalse(state.isLoading)
            }
        }

    @Test
    fun `given digitação rápida de busca, when o debounce expira, then apenas a última query dispara a busca e reseta o estado`() =
        runTest {
            // given
            coEvery { getPopularMoviesUseCase(1) } returns ResultOf.Success(paged(emptyList(), page = 1, totalPages = 1))
            coEvery { searchMoviesUseCase("abc", 1) } returns
                ResultOf.Success(paged(listOf(movie(10)), page = 1, totalPages = 1))
            val viewModel = createViewModel()
            advanceUntilIdle()

            // when
            viewModel.onSearchQueryChanged("ab")
            advanceTimeBy(200)
            viewModel.onSearchQueryChanged("abc")
            advanceUntilIdle()

            // then
            coVerify(exactly = 0) { searchMoviesUseCase("ab", any()) }
            coVerify(exactly = 1) { searchMoviesUseCase("abc", 1) }

            viewModel.uiState.test {
                // descarta o valor placeholder do stateIn antes da primeira recombinação real
                awaitItem()
                val state = awaitItem()
                assertEquals("abc", state.searchQuery)
                assertEquals(listOf(movie(10)), state.movies)
                assertEquals(1, state.currentPage)
                assertNull(state.errorMessage)
            }
        }

    @Test
    fun `given filme favoritado no repositório, when observar uiState, then isFavorite reflete o Set emitido pelo Flow`() =
        runTest {
            // given
            val moviesPage1 = listOf(movie(1), movie(2))
            coEvery { getPopularMoviesUseCase(1) } returns ResultOf.Success(paged(moviesPage1, page = 1, totalPages = 1))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiState.test {
                // descarta o valor placeholder do stateIn antes da primeira recombinação real
                awaitItem()
                val initial = awaitItem()
                assertFalse(initial.movies.first { it.id == 1 }.isFavorite)
                assertFalse(initial.movies.first { it.id == 2 }.isFavorite)

                // when
                fakeFavoritesRepository.emitFavoriteIds(setOf(1))

                // then
                val updated = awaitItem()
                assertTrue(updated.movies.first { it.id == 1 }.isFavorite)
                assertFalse(updated.movies.first { it.id == 2 }.isFavorite)
            }
        }

    @Test
    fun `given um filme exibido, when onFavoriteClick, then toggleFavoriteUseCase é chamado com o filme`() =
        runTest {
            // given
            coEvery { getPopularMoviesUseCase(1) } returns ResultOf.Success(paged(emptyList(), page = 1, totalPages = 1))
            val viewModel = createViewModel()
            advanceUntilIdle()
            val targetMovie = movie(5)

            // when
            viewModel.onFavoriteClick(targetMovie)
            advanceUntilIdle()

            // then
            coVerify { toggleFavoriteUseCase(targetMovie) }
        }
}
