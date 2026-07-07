package com.movieflux.core.common

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val failure: Failure) : UiState<Nothing>()
    data object Empty : UiState<Nothing>()
}
