package com.movieflux.feature.auth.presentation

import app.cash.turbine.test
import com.movieflux.core.common.Failure
import com.movieflux.core.common.ResultOf
import com.movieflux.domain.auth.LoginUseCase
import com.movieflux.domain.auth.SetBiometricEnabledUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val loginUseCase: LoginUseCase = mockk()
    private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase = mockk(relaxUnitFun = true)
    private val biometricCapabilityChecker: BiometricCapabilityChecker = mockk()

    private fun createViewModel() =
        AuthViewModel(
            loginUseCase = loginUseCase,
            setBiometricEnabledUseCase = setBiometricEnabledUseCase,
            biometricCapabilityChecker = biometricCapabilityChecker,
        )

    @Test
    fun `given credenciais válidas, when login, then isLoading volta a false e loginSucceeded reflete o checker de biometria`() =
        runTest {
            // given
            coEvery { loginUseCase(any(), any()) } returns ResultOf.Success(Unit)
            every { biometricCapabilityChecker.canAuthenticate() } returns true
            val viewModel = createViewModel()
            viewModel.onUsernameChanged("admin")
            viewModel.onPasswordChanged("1234")

            viewModel.uiState.test {
                // then (estado inicial após as mudanças de campo, antes do login)
                assertEquals(LoginUiState(username = "admin", password = "1234"), awaitItem())

                // when
                viewModel.login()

                // then
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)

                val finalState = awaitItem()
                assertFalse(finalState.isLoading)
                assertTrue(finalState.loginSucceeded)
                assertTrue(finalState.shouldOfferBiometric)
                assertNull(finalState.errorMessage)
            }
        }

    @Test
    fun `given checker de biometria indisponível, when login com sucesso, then shouldOfferBiometric permanece false`() =
        runTest {
            // given
            coEvery { loginUseCase(any(), any()) } returns ResultOf.Success(Unit)
            every { biometricCapabilityChecker.canAuthenticate() } returns false
            val viewModel = createViewModel()

            // when
            viewModel.login()
            advanceUntilIdle()

            // then
            val state = viewModel.uiState.value
            assertTrue(state.loginSucceeded)
            assertFalse(state.shouldOfferBiometric)
        }

    @Test
    fun `given credenciais inválidas, when login, then errorMessage é preenchido e loginSucceeded permanece false`() =
        runTest {
            // given
            coEvery { loginUseCase(any(), any()) } returns ResultOf.Error(Failure.Http(401))
            val viewModel = createViewModel()
            viewModel.onUsernameChanged("admin")
            viewModel.onPasswordChanged("senha-errada")

            // when
            viewModel.login()
            advanceUntilIdle()

            // then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.loginSucceeded)
            assertEquals("Usuário ou senha inválidos", state.errorMessage)
        }

    @Test
    fun `given errorMessage preenchido, when alterar username, then errorMessage é limpo`() =
        runTest {
            // given
            coEvery { loginUseCase(any(), any()) } returns ResultOf.Error(Failure.Http(401))
            val viewModel = createViewModel()
            viewModel.login()
            advanceUntilIdle()
            assertEquals("Usuário ou senha inválidos", viewModel.uiState.value.errorMessage)

            // when
            viewModel.onUsernameChanged("novo-usuario")

            // then
            assertNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `given errorMessage preenchido, when alterar password, then errorMessage é limpo`() =
        runTest {
            // given
            coEvery { loginUseCase(any(), any()) } returns ResultOf.Error(Failure.Http(401))
            val viewModel = createViewModel()
            viewModel.login()
            advanceUntilIdle()
            assertEquals("Usuário ou senha inválidos", viewModel.uiState.value.errorMessage)

            // when
            viewModel.onPasswordChanged("nova-senha")

            // then
            assertNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `given biometria oferecida, when onBiometricPreferenceSelected, then use case é chamado e shouldOfferBiometric é zerado`() =
        runTest {
            // given
            coEvery { loginUseCase(any(), any()) } returns ResultOf.Success(Unit)
            every { biometricCapabilityChecker.canAuthenticate() } returns true
            val viewModel = createViewModel()
            viewModel.login()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.shouldOfferBiometric)

            // when
            viewModel.onBiometricPreferenceSelected(true)
            advanceUntilIdle()

            // then
            coVerify { setBiometricEnabledUseCase(true) }
            assertFalse(viewModel.uiState.value.shouldOfferBiometric)
        }
}
