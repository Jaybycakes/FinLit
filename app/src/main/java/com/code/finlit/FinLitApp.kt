package com.code.finlit

import android.app.Application
import com.code.finlit.data.remote.ScamApi
import com.code.finlit.data.remote.UserApi
import com.code.finlit.data.repository.ScamRepositoryImpl
import com.code.finlit.data.repository.UserRepositoryImpl
import com.code.finlit.domain.repository.ScamRepository
import com.code.finlit.domain.repository.UserRepository
import com.code.finlit.presentation.scam.ScamScannerViewModel
import com.code.finlit.presentation.users.UsersViewModel
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
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module

class FinLitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@FinLitApp)
            modules(networkModule, appModule)
        }
    }
}

val networkModule = module {
    // Shared HttpClient configuration
    fun createHttpClient(baseUrl: String): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                    prettyPrint = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000  // 60 seconds for slow API
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 60_000   // 60 seconds for slow API
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        android.util.Log.d("Ktor", message)
                    }
                }
                level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
            }
            defaultRequest {
                url(baseUrl)
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }
    }
    
    // User API HttpClient
    single<HttpClient>(named("userClient")) {
        createHttpClient("https://jsonplaceholder.typicode.com/")
    }
    
    // Scam API HttpClient
    single<HttpClient>(named("scamClient")) {
        createHttpClient("https://7iffdm.buildship.run/")
    }
    
    // User API Ktorfit
    single<Ktorfit>(named("userApi")) {
        val client: HttpClient = get(named("userClient"))
        Ktorfit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .httpClient(client)
            .build()
    }
    
    // Scam API Ktorfit
    single<Ktorfit>(named("scamApi")) {
        val client: HttpClient = get(named("scamClient"))
        Ktorfit.Builder()
            .baseUrl("https://7iffdm.buildship.run/")
            .httpClient(client)
            .build()
    }
    
    // API Interfaces
    single<UserApi> {
        val ktorfit: Ktorfit = get(named("userApi"))
        ktorfit.create()
    }
    
    single<ScamApi> {
        val ktorfit: Ktorfit = get(named("scamApi"))
        ktorfit.create()
    }
}

val appModule = module {
    // Repositories
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<ScamRepository> { ScamRepositoryImpl(get()) }
    
    // ViewModels
    viewModel { UsersViewModel(get()) }
    viewModel { ScamScannerViewModel(get()) }
}
