# Ktorfit Network Layer

## API Interface

```kotlin
// data/remote/UserApi.kt
interface UserApi {
    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): UserDto

    @POST("users")
    suspend fun createUser(@Body body: CreateUserRequest): UserDto

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body body: UpdateUserRequest): UserDto

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int)

    // Query params
    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String, @Query("page") page: Int = 1): List<UserDto>
}
```

## DTOs (Data Transfer Objects)

```kotlin
// data/remote/dto/UserDto.kt
@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class CreateUserRequest(val name: String, val email: String)

@Serializable
data class UpdateUserRequest(val name: String? = null, val email: String? = null)
```

## HttpClient Setup (Production)

```kotlin
// di/NetworkModule.kt
@Module
@ComponentScan("com.example.myapp")
class NetworkModule {

    @Single
    fun provideHttpClient(): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true   // resilient to API adding new fields
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
        // Auth header — swap for your token source
        defaultRequest {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            // header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    @Single
    fun provideKtorfit(client: HttpClient): Ktorfit = Ktorfit.Builder()
        .baseUrl("https://api.example.com/v1/")
        .httpClient(client)
        .build()

    @Single
    fun provideUserApi(ktorfit: Ktorfit): UserApi = ktorfit.create()
}
```

## SSL: Debug Builds — Trust All Certificates

Use ONLY in development/debug. Never ship this in production.

```kotlin
// di/NetworkModule.kt  (inside the @Module class)
@Single
fun provideHttpClient(): HttpClient {
    val engine = if (BuildConfig.DEBUG) unsafeAndroidEngine() else Android.create()
    return HttpClient(engine) {
        // ... same installs as above
    }
}

// utils/UnsafeHttpEngine.kt
fun unsafeAndroidEngine(): HttpClientEngine {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    })
    val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, trustAllCerts, java.security.SecureRandom())
    }
    val okHttpClient = OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .build()
    return OkHttp.create(okHttpClient)
}
```

Required imports for unsafeAndroidEngine:
```kotlin
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
```

## SSL: Production — Certificate Pinning

```kotlin
// utils/PinnedHttpEngine.kt
fun pinnedAndroidEngine(vararg pins: String): HttpClientEngine {
    val pinner = CertificatePinner.Builder().apply {
        // Get pin: openssl s_client -connect api.example.com:443 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform DER | openssl dgst -sha256 -binary | openssl enc -base64
        pins.forEach { add("api.example.com", "sha256/$it") }
    }.build()
    val okHttpClient = OkHttpClient.Builder()
        .certificatePinner(pinner)
        .build()
    return OkHttp.create(okHttpClient)
}
```

Usage in NetworkModule:
```kotlin
@Single
fun provideHttpClient(): HttpClient = HttpClient(
    pinnedAndroidEngine("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
) { /* installs */ }
```

## Network Error Handling

Define a sealed error type to map all network failures:

```kotlin
// domain/error/NetworkError.kt
sealed class NetworkError : Exception() {
    data object NoInternet : NetworkError()
    data object Timeout : NetworkError()
    data class ServerError(val code: Int, val body: String?) : NetworkError()
    data class UnknownError(override val cause: Throwable) : NetworkError()
}
```

Extension to map Ktor exceptions:

```kotlin
// data/remote/NetworkErrorMapper.kt
fun Throwable.toNetworkError(): NetworkError = when (this) {
    is UnresolvedAddressException -> NetworkError.NoInternet
    is ConnectTimeoutException,
    is SocketTimeoutException,
    is HttpRequestTimeoutException -> NetworkError.Timeout
    is ClientRequestException -> NetworkError.ServerError(response.status.value, null)
    is ServerResponseException -> NetworkError.ServerError(response.status.value, null)
    else -> NetworkError.UnknownError(this)
}
```

## Repository Pattern with Error Handling

```kotlin
// domain/repository/UserRepository.kt
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUser(id: Int): Result<User>
}

// data/repository/UserRepositoryImpl.kt
@Single(binds = [UserRepository::class])
class UserRepositoryImpl(private val api: UserApi) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> = runCatching {
        api.getUsers().map { it.toDomain() }
    }.mapFailure { it.toNetworkError() }

    override suspend fun getUser(id: Int): Result<User> = runCatching {
        api.getUser(id).toDomain()
    }.mapFailure { it.toNetworkError() }
}

// Mapper
fun UserDto.toDomain(): User = User(id = id, name = name, email = email)

// Helper: map Result failure
fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
    onFailure { return Result.failure(transform(it)) }.let { this }
```
