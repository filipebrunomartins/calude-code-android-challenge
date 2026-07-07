package com.movieflux.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ApiKeyInterceptor
    @Inject
    constructor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val urlWithApiKey =
                originalRequest.url
                    .newBuilder()
                    .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
                    .build()

            return chain.proceed(originalRequest.newBuilder().url(urlWithApiKey).build())
        }
    }
