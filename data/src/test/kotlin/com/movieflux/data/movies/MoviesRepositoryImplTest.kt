package com.movieflux.data.movies

import com.movieflux.core.common.Failure
import com.movieflux.core.common.ResultOf
import com.movieflux.data.movies.remote.TmdbApiService
import com.movieflux.data.movies.remote.dto.GenreDto
import com.movieflux.data.movies.remote.dto.MovieDetailDto
import com.movieflux.data.movies.remote.dto.MovieDto
import com.movieflux.data.movies.remote.dto.MoviePageResponseDto
import com.movieflux.domain.movies.GenreRepository
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class MoviesRepositoryImplTest {
    private val tmdbApiService: TmdbApiService = mockk()
    private val genreRepository: GenreRepository = mockk()

    private fun repository() = MoviesRepositoryImpl(tmdbApiService, genreRepository)

    private fun movieDto(
        id: Int,
        genreIds: List<Int> = listOf(28),
    ) = MovieDto(
        id = id,
        title = "Filme $id",
        overview = "Sinopse do filme $id",
        posterPath = "/poster$id.jpg",
        backdropPath = "/backdrop$id.jpg",
        voteAverage = 7.5,
        genreIds = genreIds,
    )

    private fun httpException(code: Int): HttpException {
        val body = "erro".toResponseBody(null)
        return HttpException(Response.error<Any>(code, body))
    }

    @Test
    fun `given resposta de sucesso da API, when getPopularMovies, then mapeia para PagedResult aplicando os gêneros do repositório`() =
        runTest {
            // given
            val genreMap = mapOf(28 to "Ação")
            coEvery { genreRepository.refreshGenresIfNeeded() } returns ResultOf.Success(Unit)
            every { genreRepository.observeGenreMap() } returns flowOf(genreMap)
            coEvery { tmdbApiService.getPopularMovies(1) } returns
                MoviePageResponseDto(
                    page = 1,
                    results = listOf(movieDto(1), movieDto(2)),
                    totalPages = 5,
                    totalResults = 100,
                )

            // when
            val result = repository().getPopularMovies(1)

            // then
            assertTrue(result is ResultOf.Success)
            val paged = (result as ResultOf.Success).data
            assertEquals(1, paged.page)
            assertEquals(5, paged.totalPages)
            assertEquals(listOf(1, 2), paged.items.map { it.id })
            assertEquals(listOf("Ação"), paged.items[0].genres)
            assertEquals(listOf("Ação"), paged.items[1].genres)
        }

    @Test
    fun `given getPopularMovies chamado, when busca o mapa de gêneros, then refreshGenresIfNeeded é chamado antes de observeGenreMap`() =
        runTest {
            // given
            coEvery { genreRepository.refreshGenresIfNeeded() } returns ResultOf.Success(Unit)
            every { genreRepository.observeGenreMap() } returns flowOf(emptyMap())
            coEvery { tmdbApiService.getPopularMovies(1) } returns
                MoviePageResponseDto(page = 1, results = emptyList(), totalPages = 1)

            // when
            repository().getPopularMovies(1)

            // then
            coVerifyOrder {
                genreRepository.refreshGenresIfNeeded()
                genreRepository.observeGenreMap()
            }
        }

    @Test
    fun `given IOException lançada pela API, when getPopularMovies, then retorna Failure NoConnection`() =
        runTest {
            // given
            coEvery { genreRepository.refreshGenresIfNeeded() } returns ResultOf.Success(Unit)
            every { genreRepository.observeGenreMap() } returns flowOf(emptyMap())
            coEvery { tmdbApiService.getPopularMovies(1) } throws IOException("sem conexão")

            // when
            val result = repository().getPopularMovies(1)

            // then
            assertEquals(ResultOf.Error(Failure.NoConnection), result)
        }

    @Test
    fun `given HttpException 404 lançada pela API, when getPopularMovies, then retorna Failure Http 404`() =
        runTest {
            // given
            coEvery { genreRepository.refreshGenresIfNeeded() } returns ResultOf.Success(Unit)
            every { genreRepository.observeGenreMap() } returns flowOf(emptyMap())
            coEvery { tmdbApiService.getPopularMovies(1) } throws httpException(404)

            // when
            val result = repository().getPopularMovies(1)

            // then
            assertEquals(ResultOf.Error(Failure.Http(404)), result)
        }

    @Test
    fun `given resposta de sucesso da API, when searchMovies, then mapeia para PagedResult aplicando os gêneros do repositório`() =
        runTest {
            // given
            val genreMap = mapOf(12 to "Aventura")
            coEvery { genreRepository.refreshGenresIfNeeded() } returns ResultOf.Success(Unit)
            every { genreRepository.observeGenreMap() } returns flowOf(genreMap)
            coEvery { tmdbApiService.searchMovies("batman", 1) } returns
                MoviePageResponseDto(
                    page = 1,
                    results = listOf(movieDto(id = 10, genreIds = listOf(12))),
                    totalPages = 2,
                    totalResults = 20,
                )

            // when
            val result = repository().searchMovies("batman", 1)

            // then
            assertTrue(result is ResultOf.Success)
            val paged = (result as ResultOf.Success).data
            assertEquals(1, paged.page)
            assertEquals(2, paged.totalPages)
            assertEquals(listOf(10), paged.items.map { it.id })
            assertEquals(listOf("Aventura"), paged.items[0].genres)
        }

    @Test
    fun `given IOException lançada pela API, when searchMovies, then retorna Failure NoConnection`() =
        runTest {
            // given
            coEvery { genreRepository.refreshGenresIfNeeded() } returns ResultOf.Success(Unit)
            every { genreRepository.observeGenreMap() } returns flowOf(emptyMap())
            coEvery { tmdbApiService.searchMovies("batman", 1) } throws IOException("sem conexão")

            // when
            val result = repository().searchMovies("batman", 1)

            // then
            assertEquals(ResultOf.Error(Failure.NoConnection), result)
        }

    @Test
    fun `given MovieDetailDto retornado pela API, when getMovieDetails, then mapeia para Movie sem consultar o genreRepository`() =
        runTest {
            // given
            coEvery { tmdbApiService.getMovieDetails(42) } returns
                MovieDetailDto(
                    id = 42,
                    title = "Filme 42",
                    overview = "Sinopse do filme 42",
                    posterPath = "/poster42.jpg",
                    backdropPath = "/backdrop42.jpg",
                    voteAverage = 8.2,
                    genres = listOf(GenreDto(id = 28, name = "Ação"), GenreDto(id = 12, name = "Aventura")),
                )

            // when
            val result = repository().getMovieDetails(42)

            // then
            assertTrue(result is ResultOf.Success)
            val movie = (result as ResultOf.Success).data
            assertEquals(42, movie.id)
            assertEquals("Filme 42", movie.title)
            assertEquals(listOf("Ação", "Aventura"), movie.genres)
            verify { genreRepository wasNot Called }
        }

    @Test
    fun `given HttpException 404 lançada pela API, when getMovieDetails, then retorna Failure Http 404`() =
        runTest {
            // given
            coEvery { tmdbApiService.getMovieDetails(42) } throws httpException(404)

            // when
            val result = repository().getMovieDetails(42)

            // then
            assertEquals(ResultOf.Error(Failure.Http(404)), result)
        }
}
