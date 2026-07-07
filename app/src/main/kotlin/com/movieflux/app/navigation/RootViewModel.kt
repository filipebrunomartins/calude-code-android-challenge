package com.movieflux.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieflux.core.navigation.Route
import com.movieflux.domain.auth.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
) : ViewModel() {

    val startDestination: StateFlow<Route?> = observeSessionUseCase()
        .map { session ->
            when {
                !session.isLoggedIn -> Route.Login
                session.biometricEnabled -> Route.BiometricGate
                else -> Route.Home
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
