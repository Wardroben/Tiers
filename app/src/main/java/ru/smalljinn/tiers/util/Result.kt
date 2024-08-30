package ru.smalljinn.tiers.util


sealed class Result<T>(val data: T? = null, message: String? = null) {
    class Success<T>(data: T?) : Result<T>(data)
    class Error<T>(val message: String?, data: T? = null) : Result<T>(data, message)
    data class Loading<T>(val isLoading: Boolean = true) : Result<T>(data = null)
}