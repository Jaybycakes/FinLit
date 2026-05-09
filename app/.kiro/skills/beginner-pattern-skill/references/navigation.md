# Navigation (Navigation 3 + Koin)

Navigation 3 uses a list-based backstack with `@Serializable` data classes as routes.

## Route Definitions

```kotlin
// navigation/Routes.kt
@Serializable
data object UsersRoute

@Serializable
data class UserDetailRoute(val userId: Int)

@Serializable
data object CreateUserRoute
```

## NavHost Setup

```kotlin
// navigation/AppNavHost.kt
@Composable
fun AppNavHost() {
    val backStack = rememberNavBackStack(UsersRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberKoinNavEntryDecorator(),  // Koin scoping per back-stack entry
        ),
    ) { entry ->
        when (val route = entry.key) {
            is UsersRoute -> UsersRoute(
                onNavigateToDetail = { userId ->
                    backStack.add(UserDetailRoute(userId))
                },
                onNavigateToCreate = { backStack.add(CreateUserRoute) },
            )
            is UserDetailRoute -> UserDetailRoute(
                userId = route.userId,
                onBack = { backStack.removeLastOrNull() },
            )
            is CreateUserRoute -> CreateUserRoute(
                onBack = { backStack.removeLastOrNull() },
                onCreated = { backStack.removeLastOrNull() },
            )
        }
    }
}
```

## MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AppNavHost()
            }
        }
    }
}
```

## Receiving Arguments in ViewModel

Navigation 3 passes route objects directly. Extract args from `SavedStateHandle` or receive via constructor:

```kotlin
// Option A: SavedStateHandle (recommended for Koin @KoinViewModel)
@KoinViewModel
class UserDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: UserRepository,
) : ViewModel(), ContainerHost<UserDetailState, UserDetailSideEffect> {
    private val userId: Int = checkNotNull(savedStateHandle["userId"])
    // ...
}
```

```kotlin
// Option B: Pass directly as assisted injection (advanced)
// Prefer Option A for simplicity with beginners
```

## Backstack — Minimum Identifier Principle

```kotlin
// CORRECT — pass only the ID
backStack.add(UserDetailRoute(userId = user.id))

// WRONG — don't serialize entire objects into routes
// backStack.add(UserDetailRoute(user = user))  // can cause TransactionTooLargeException
```

The destination ViewModel fetches fresh data using the ID. This ensures data is always current.

## Deep Links (optional)

```kotlin
@Serializable
@DeepLink(basePath = "https://myapp.com/users")
data class UsersRoute

@Serializable
@DeepLink(basePath = "https://myapp.com/users/{userId}")
data class UserDetailRoute(val userId: Int)
```

AndroidManifest.xml:
```xml
<activity android:name=".MainActivity">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https" android:host="myapp.com" />
    </intent-filter>
</activity>
```
