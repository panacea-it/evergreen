package com.prod.evergreen.api

sealed class Resource<T> (val data :T?=null, val message:String?=null, val statusCode: Int? = null)
{
    class Success<T>(data :T): Resource<T>(data)
    class Error<T>(message: String?,data: T?=null,statusCode: Int? = null): Resource<T>(data, message, statusCode)
    class Loading<T>: Resource<T>()

}