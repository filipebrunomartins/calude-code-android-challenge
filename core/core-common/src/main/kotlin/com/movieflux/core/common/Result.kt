package com.movieflux.core.common

sealed class ResultOf<out T> {
    data class Success<T>(val data: T) : ResultOf<T>()
    data class Error(val failure: Failure) : ResultOf<Nothing>()
}

inline fun <T, R> ResultOf<T>.map(transform: (T) -> R): ResultOf<R> = when (this) {
    is ResultOf.Success -> ResultOf.Success(transform(data))
    is ResultOf.Error -> this
}
