package com.movieflux.domain.auth

import com.movieflux.core.common.Failure
import com.movieflux.core.common.ResultOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {
    @Test
    fun `given credenciais válidas, when login, then retorna sucesso`() =
        runTest {
            // given
            val repository = FakeAuthRepository()
            repository.loginResult = ResultOf.Success(Unit)
            val useCase = LoginUseCase(repository)

            // when
            val result = useCase("admin", "1234")

            // then
            assertTrue(result is ResultOf.Success)
            assertEquals("admin" to "1234", repository.loginCalls.single())
        }

    @Test
    fun `given credenciais inválidas, when login, then retorna erro`() =
        runTest {
            // given
            val repository = FakeAuthRepository()
            repository.loginResult = ResultOf.Error(Failure.Http(401))
            val useCase = LoginUseCase(repository)

            // when
            val result = useCase("admin", "senha-errada")

            // then
            assertTrue(result is ResultOf.Error)
            assertEquals(Failure.Http(401), (result as ResultOf.Error).failure)
        }

    @Test
    fun `given chamada de login, when invoke, then repositório recebe username e password corretos`() =
        runTest {
            // given
            val repository = FakeAuthRepository()
            val useCase = LoginUseCase(repository)

            // when
            useCase("user123", "senha-secreta")

            // then
            assertEquals(listOf("user123" to "senha-secreta"), repository.loginCalls)
        }
}
