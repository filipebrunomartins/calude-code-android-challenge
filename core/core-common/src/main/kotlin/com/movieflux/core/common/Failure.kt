package com.movieflux.core.common

sealed class Failure {
    data object NoConnection : Failure()

    data object Timeout : Failure()

    data class Http(
        val code: Int,
    ) : Failure()

    data object Unknown : Failure()
}
