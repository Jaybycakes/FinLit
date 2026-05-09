package com.code.finlit.domain.error

sealed class NetworkError : Exception() {
    data object NoInternet : NetworkError()
    data object Timeout : NetworkError()
    data class ServerError(val code: Int, val body: String?) : NetworkError()
    data class UnknownError(override val cause: Throwable) : NetworkError()
}
