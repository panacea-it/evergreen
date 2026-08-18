package com.prod.evergreen.api

import retrofit2.Response


sealed class NetworkState<out T> {
    data class Success<out T>(val data: T) : NetworkState<T>()
    data class Error<T>(val message: String? = null,val statusCode: Int? = null) : NetworkState<T>()
}

fun <T> Response<T>.parseResponse(): NetworkState<T> {
    return if (isSuccessful && body() != null) {
        NetworkState.Success(body()!!)
    } else {
        NetworkState.Error(errorBody()?.string() ?: message(), code())
    }
}
