package com.yeloe.attentanceapp.utils

sealed class Resources<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resources<T>(data)
    class Error<T>(message: String, data: T? = null) : Resources<T>(data, message)
    class Loading<T> : Resources<T>()
    class Completed<T>: Resources<T>()
}