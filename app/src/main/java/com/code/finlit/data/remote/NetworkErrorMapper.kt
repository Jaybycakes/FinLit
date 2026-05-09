package com.code.finlit.data.remote

import com.code.finlit.domain.error.NetworkError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

fun Throwable.toNetworkError(): NetworkError = when (this) {
    is UnresolvedAddressException,
    is UnknownHostException,
    is ConnectException -> NetworkError.NoInternet
    is ConnectException,
    is SocketTimeoutException,
    is HttpRequestTimeoutException -> NetworkError.Timeout
    is ClientRequestException -> NetworkError.ServerError(response.status.value, null)
    is ServerResponseException -> NetworkError.ServerError(response.status.value, null)
    else -> NetworkError.UnknownError(this)
}

fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
    onFailure { return Result.failure(transform(it)) }.let { this }
