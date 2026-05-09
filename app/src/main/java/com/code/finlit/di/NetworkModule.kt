package com.code.finlit.di

import android.util.Log
import com.code.finlit.BuildConfig
import com.code.finlit.data.remote.UserApi
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.code.finlit")
class NetworkModule {

    @Single
    fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("Ktor", message)
                }
            }
            level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
        }
        defaultRequest {
            url("https://jsonplaceholder.typicode.com/")
            headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }
    }

    @Single
    fun provideKtorfit(client: HttpClient): Ktorfit = Ktorfit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .httpClient(client)
        .build()

    @Single
    fun provideUserApi(ktorfit: Ktorfit): UserApi = ktorfit.create()
}
