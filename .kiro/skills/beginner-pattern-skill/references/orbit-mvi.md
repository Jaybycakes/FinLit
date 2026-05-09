# Orbit MVI

## Contract (State + Intent + SideEffect)

Always define these three in a single Contract file per screen.

```kotlin
// presentation/users/UsersContract.kt
data class UsersState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null,
)

sealed interface UsersIntent {
    data object LoadUsers : UsersIntent
    data class DeleteUser(val id: Int) : UsersIntent
    data class SearchUsers(val query: String) : UsersIntent
}

sealed interface UsersSideEffect {
    data class ShowMessage(val message: String) : UsersSideEffect
    data class NavigateToDetail(val userId: Int) : UsersSideEffect
}
```

## ViewModel

```kotlin
// presentation/users/UsersViewModel.kt
@KoinViewModel
class UsersViewModel(
    private val userRepository: UserRepository,
) : ViewModel(), ContainerHost<UsersState, UsersSideEffect> {

    override val container = container<UsersState, UsersSideEffect>(UsersState())

    init {
        onIntent(UsersIntent.LoadUsers)
    }

    fun onIntent(intent: UsersIntent) = when (intent) {
        is UsersIntent.LoadUsers -> loadUsers()
        is UsersIntent.DeleteUser -> deleteUser(intent.id)
        is UsersIntent.SearchUsers -> searchUsers(intent.query)
    }

    private fun loadUsers() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        userRepository.getUsers()
            .onSuccess { users ->
                reduce { state.copy(isLoading = false, users = users) }
            }
            .onFailure { error ->
                reduce { state.copy(isLoading = false) }
                postSideEffect(UsersSideEffect.ShowMessage(error.toUserMessage()))
            }
    }

    private fun deleteUser(id: Int) = intent {
        userRepository.deleteUser(id)
            .onSuccess {
                reduce { state.copy(users = state.users.filter { it.id != id }) }
                postSideEffect(UsersSideEffect.ShowMessage("User deleted"))
            }
            .onFailure { error ->
                postSideEffect(UsersSideEffect.ShowMessage(error.toUserMessage()))
            }
    }

    private fun searchUsers(query: String) = intent {
        if (query.isBlank()) {
            loadUsers()
            return@intent
        }
        reduce { state.copy(isLoading = true, error = null) }
        userRepository.searchUsers(query)
            .onSuccess { users -> reduce { state.copy(isLoading = false, users = users) } }
            .onFailure { error ->
                reduce { state.copy(isLoading = false) }
                postSideEffect(UsersSideEffect.ShowMessage(error.toUserMessage()))
            }
    }
}

// Map domain errors to human-readable strings
fun Throwable.toUserMessage(): String = when (this) {
    is NetworkError.NoInternet -> "No internet connection"
    is NetworkError.Timeout -> "Request timed out. Try again."
    is NetworkError.ServerError -> "Server error (${code}). Try again later."
    else -> "Something went wrong. Try again."
}
```

## UiState Pattern (alternative to flat State)

For screens with very distinct Loading/Success/Error visuals:

```kotlin
data class UsersState(
    val uiState: UiState<List<User>> = UiState.Loading,
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

// In ViewModel:
reduce { state.copy(uiState = UiState.Loading) }
reduce { state.copy(uiState = UiState.Success(users)) }
reduce { state.copy(uiState = UiState.Error(error.toUserMessage())) }
```

## Key Rules

- `reduce { }` — synchronous, atomic state mutation. Never call suspend functions inside.
- `intent { }` — coroutine scope for async work. Always wrap in `intent { }` if you call suspend functions.
- `postSideEffect()` — one-time events. Cached until observed once, then discarded.
- All intent functions are sequential by default — no race conditions on `state`.
- Never expose `MutableStateFlow` from ViewModel. Orbit manages state internally.
- `container` is created once; ViewModel lifecycle matches the Compose back-stack entry.
