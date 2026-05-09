# Koin Dependency Injection

## Module with Annotations

```kotlin
// di/AppModule.kt
@Module
@ComponentScan("com.example.myapp")   // scans the whole package for @Single/@Factory/@KoinViewModel
class AppModule
```

`@ComponentScan` auto-discovers all annotated classes — you don't need to list each one.

## Annotating Classes

```kotlin
// Singleton — one instance for app lifetime
@Single
class UserRepositoryImpl(private val api: UserApi) : UserRepository

// Factory — new instance every injection
@Factory
class SomeUseCase(private val repository: UserRepository)

// ViewModel — lifecycle-aware, scoped to back-stack entry
@KoinViewModel
class UsersViewModel(private val repository: UserRepository) : ViewModel(), ContainerHost<...>
```

## Binding Interfaces

```kotlin
// Binds UserRepositoryImpl to the UserRepository interface
@Single(binds = [UserRepository::class])
class UserRepositoryImpl(private val api: UserApi) : UserRepository
```

## NetworkModule (manual DSL — required for HttpClient construction)

Koin Annotations can't annotate third-party classes. Use DSL for the network graph:

```kotlin
// di/NetworkModule.kt
@Module
@ComponentScan("com.example.myapp")
class NetworkModule {
    @Single
    fun provideHttpClient(): HttpClient = HttpClient(Android) { /* see ktorfit-network.md */ }

    @Single
    fun provideKtorfit(client: HttpClient): Ktorfit = Ktorfit.Builder()
        .baseUrl("https://api.example.com/v1/")
        .httpClient(client)
        .build()

    @Single
    fun provideUserApi(ktorfit: Ktorfit): UserApi = ktorfit.create()
}
```

## Application Class

```kotlin
// MyApp.kt
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)   // use Level.ERROR in release
            androidContext(this@MyApp)
            modules(
                AppModule().module,
                NetworkModule().module,
            )
        }
    }
}
```

Register in AndroidManifest.xml:
```xml
<application android:name=".MyApp" ...>
```

## Koin Compiler Plugin — KSP Config

```kotlin
// app/build.gradle.kts
ksp {
    arg("KOIN_CONFIG_CHECK", "true")   // compile-time graph validation
}
```

## Injecting ViewModel in Compose

```kotlin
// In any Composable
@Composable
fun UsersScreen(viewModel: UsersViewModel = koinViewModel()) {
    // ...
}
```

With navigation arguments (SavedStateHandle auto-populated):
```kotlin
@KoinViewModel
class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: UserRepository,
) : ViewModel(), ContainerHost<...> {
    private val userId: Int = checkNotNull(savedStateHandle["userId"])
    init { loadUser(userId) }
}
```

## Scopes (Advanced)

For flows where objects should live longer than a screen but shorter than the app:

```kotlin
@Single(scope = CheckoutScope::class)
class CartManager

// Open/close scope in ViewModel or Navigation event
val scope = getKoin().createScope("checkoutScopeId", named<CheckoutScope>())
scope.close()
```

## Koin Verify Test (unit test)

```kotlin
class KoinVerifyTest : KoinTest {
    @Test
    fun verifyKoinModules() {
        appModule.verify(
            extraTypes = listOf(Application::class, SavedStateHandle::class)
        )
    }
}
```
