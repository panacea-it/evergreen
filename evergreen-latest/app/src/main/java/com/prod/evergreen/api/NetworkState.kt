package com.prod.evergreen.api

import com.google.gson.JsonParser
import retrofit2.Response


sealed class NetworkState<out T> {
    data class Success<out T>(val data: T) : NetworkState<T>()
    data class Error<T>(val message: String? = null,val statusCode: Int? = null) : NetworkState<T>()
}

private fun extractApiMessage(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        val json = JsonParser.parseString(raw).asJsonObject
        json.get("message")?.asString ?: raw
    } catch (_: Exception) {
        raw
    }
}

fun <T> Response<T>.parseResponse(): NetworkState<T> {
    return if (isSuccessful && body() != null) {
        NetworkState.Success(body()!!)
    } else {
        val rawError = errorBody()?.string()
        NetworkState.Error(extractApiMessage(rawError) ?: message(), code())
    }
}
