package com.movieflux.core.network

import com.movieflux.core.common.Failure
import com.movieflux.core.common.ResultOf
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

@Suppress("TooGenericExceptionCaught", "SwallowedException")
suspend fun <T> safeApiCall(apiCall: suspend () -> T): ResultOf<T> =
    try {
        ResultOf.Success(apiCall())
    } catch (e: SocketTimeoutException) {
        ResultOf.Error(Failure.Timeout)
    } catch (e: IOException) {
        ResultOf.Error(Failure.NoConnection)
    } catch (e: HttpException) {
        ResultOf.Error(Failure.Http(e.code()))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ResultOf.Error(Failure.Unknown)
    }
