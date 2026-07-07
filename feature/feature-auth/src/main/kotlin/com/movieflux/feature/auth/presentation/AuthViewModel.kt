package com.movieflux.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieflux.core.common.ResultOf
import com.movieflux.domain.auth.LoginUseCase
import com.movieflux.domain.auth.SetBiometricEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSucceeded: Boolean = false,
    val shouldOfferBiometric: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
    private val biometricCapabilityChecker: BiometricCapabilityChecker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (loginUseCase(state.username, state.password)) {
                is ResultOf.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginSucceeded = true,
                        shouldOfferBiometric = biometricCapabilityChecker.canAuthenticate(),
                    )
                }
                is ResultOf.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Usuário ou senha inválidos")
                }
            }
        }
    }

    fun onBiometricPreferenceSelected(enabled: Boolean) {
        viewModelScope.launch {
            setBiometricEnabledUseCase(enabled)
            _uiState.update { it.copy(shouldOfferBiometric = false) }
        }
    }
}
