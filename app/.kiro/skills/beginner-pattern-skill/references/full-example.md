# Full End-to-End Example: Users Feature

Complete working code for a Users screen: list → detail → create.

## Domain Layer

```kotlin
// domain/model/User.kt
data class User(
    val id: Int,
    val name: String,
    val email: String,
)

// domain/repository/UserRepository.kt
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUser(id: Int): Result<User>
    suspend fun createUser(name: String, email: String): Result<User>
    suspend fun deleteUser(id: Int): Result<Unit>
}
```

## Data Layer

```kotlin
// data/remote/UserApi.kt
interface UserApi {
    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): UserDto

    @POST("users")
    suspend fun createUser(@Body body: CreateUserRequest): UserDto

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int)
}

// data/remote/dto/UserDto.kt
@Serializable
data class UserDto(val id: Int, val name: String, val email: String)

@Serializable
data class CreateUserRequest(val name: String, val email: String)

// data/remote/mapper/UserMapper.kt
fun UserDto.toDomain(): User = User(id = id, name = name, email = email)

// data/repository/UserRepositoryImpl.kt
@Single(binds = [UserRepository::class])
class UserRepositoryImpl(private val api: UserApi) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> = runCatching {
        api.getUsers().map { it.toDomain() }
    }.mapFailure { it.toNetworkError() }

    override suspend fun getUser(id: Int): Result<User> = runCatching {
        api.getUser(id).toDomain()
    }.mapFailure { it.toNetworkError() }

    override suspend fun createUser(name: String, email: String): Result<User> = runCatching {
        api.createUser(CreateUserRequest(name, email)).toDomain()
    }.mapFailure { it.toNetworkError() }

    override suspend fun deleteUser(id: Int): Result<Unit> = runCatching {
        api.deleteUser(id)
    }.mapFailure { it.toNetworkError() }
}
```

## Presentation Layer — Users List

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
    data class SelectUser(val id: Int) : UsersIntent
}

sealed interface UsersSideEffect {
    data class ShowMessage(val message: String) : UsersSideEffect
    data class NavigateToDetail(val userId: Int) : UsersSideEffect
}

// presentation/users/UsersViewModel.kt
@KoinViewModel
class UsersViewModel(
    private val repository: UserRepository,
) : ViewModel(), ContainerHost<UsersState, UsersSideEffect> {

    override val container = container<UsersState, UsersSideEffect>(UsersState())

    init { onIntent(UsersIntent.LoadUsers) }

    fun onIntent(intent: UsersIntent) = when (intent) {
        UsersIntent.LoadUsers -> loadUsers()
        is UsersIntent.DeleteUser -> deleteUser(intent.id)
        is UsersIntent.SelectUser -> intent { postSideEffect(UsersSideEffect.NavigateToDetail(intent.id)) }
    }

    private fun loadUsers() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        repository.getUsers()
            .onSuccess { reduce { state.copy(isLoading = false, users = it) } }
            .onFailure {
                reduce { state.copy(isLoading = false, error = it.toUserMessage()) }
                postSideEffect(UsersSideEffect.ShowMessage(it.toUserMessage()))
            }
    }

    private fun deleteUser(id: Int) = intent {
        repository.deleteUser(id)
            .onSuccess {
                reduce { state.copy(users = state.users.filter { it.id != id }) }
                postSideEffect(UsersSideEffect.ShowMessage("Deleted successfully"))
            }
            .onFailure { postSideEffect(UsersSideEffect.ShowMessage(it.toUserMessage())) }
    }
}

// presentation/users/UsersScreen.kt
@Composable
fun UsersRoute(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: UsersViewModel = koinViewModel(),
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(viewModel) {
        viewModel.container.sideEffectFlow
            .flowWithLifecycle(lifecycle)
            .collect { effect ->
                when (effect) {
                    is UsersSideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                    is UsersSideEffect.NavigateToDetail -> onNavigateToDetail(effect.userId)
                }
            }
    }

    UsersScreen(state = state, snackbarHostState = snackbarHostState, onIntent = viewModel::onIntent)
}

@Composable
fun UsersScreen(
    state: UsersState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (UsersIntent) -> Unit = {},
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Users") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* navigate to create */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> LoadingContent()
                state.error != null -> ErrorContent(
                    message = state.error,
                    onRetry = { onIntent(UsersIntent.LoadUsers) },
                )
                state.users.isEmpty() -> EmptyContent()
                else -> UsersList(
                    users = state.users,
                    onUserClick = { onIntent(UsersIntent.SelectUser(it.id)) },
                    onDeleteClick = { onIntent(UsersIntent.DeleteUser(it.id)) },
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Users - Loading")
@Composable
private fun UsersLoadingPreview() = AppTheme { UsersScreen(UsersState(isLoading = true)) }

@Preview(showBackground = true, name = "Users - List")
@Composable
private fun UsersListPreview() = AppTheme {
    UsersScreen(
        UsersState(users = listOf(User(1, "Alice", "alice@example.com"), User(2, "Bob", "bob@example.com")))
    )
}

@Preview(showBackground = true, name = "Users - Error")
@Composable
private fun UsersErrorPreview() = AppTheme {
    UsersScreen(UsersState(error = "No internet connection"))
}

@Preview(showBackground = true, name = "Users - Empty")
@Composable
private fun UsersEmptyPreview() = AppTheme { UsersScreen(UsersState()) }
```

## Presentation Layer — Create User

```kotlin
// presentation/createuser/CreateUserContract.kt
data class CreateUserState(
    val name: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
)

sealed interface CreateUserIntent {
    data class NameChanged(val value: String) : CreateUserIntent
    data class EmailChanged(val value: String) : CreateUserIntent
    data object Submit : CreateUserIntent
}

sealed interface CreateUserSideEffect {
    data object NavigateBack : CreateUserSideEffect
    data class ShowError(val message: String) : CreateUserSideEffect
}

// presentation/createuser/CreateUserViewModel.kt
@KoinViewModel
class CreateUserViewModel(
    private val repository: UserRepository,
) : ViewModel(), ContainerHost<CreateUserState, CreateUserSideEffect> {

    override val container = container<CreateUserState, CreateUserSideEffect>(CreateUserState())

    fun onIntent(intent: CreateUserIntent) = when (intent) {
        is CreateUserIntent.NameChanged -> intent { reduce { state.copy(name = intent.value, nameError = null) } }
        is CreateUserIntent.EmailChanged -> intent { reduce { state.copy(email = intent.value, emailError = null) } }
        CreateUserIntent.Submit -> submit()
    }

    private fun submit() = intent {
        val nameError = if (state.name.isBlank()) "Name is required" else null
        val emailError = when {
            state.email.isBlank() -> "Email is required"
            !state.email.contains("@") -> "Invalid email format"
            else -> null
        }

        if (nameError != null || emailError != null) {
            reduce { state.copy(nameError = nameError, emailError = emailError) }
            return@intent
        }

        reduce { state.copy(isLoading = true) }
        repository.createUser(state.name.trim(), state.email.trim())
            .onSuccess { postSideEffect(CreateUserSideEffect.NavigateBack) }
            .onFailure {
                reduce { state.copy(isLoading = false) }
                postSideEffect(CreateUserSideEffect.ShowError(it.toUserMessage()))
            }
    }
}

// presentation/createuser/CreateUserScreen.kt
@Composable
fun CreateUserRoute(
    onBack: () -> Unit,
    viewModel: CreateUserViewModel = koinViewModel(),
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(viewModel) {
        viewModel.container.sideEffectFlow
            .flowWithLifecycle(lifecycle)
            .collect { effect ->
                when (effect) {
                    CreateUserSideEffect.NavigateBack -> onBack()
                    is CreateUserSideEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                }
            }
    }

    CreateUserScreen(state = state, snackbarHostState = snackbarHostState, onIntent = viewModel::onIntent)
}

@Composable
fun CreateUserScreen(
    state: CreateUserState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (CreateUserIntent) -> Unit = {},
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Create User") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { onIntent(CreateUserIntent.NameChanged(it)) },
                label = { Text("Name") },
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = { onIntent(CreateUserIntent.EmailChanged(it)) },
                label = { Text("Email") },
                isError = state.emailError != null,
                supportingText = state.emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Button(
                onClick = { onIntent(CreateUserIntent.Submit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Create - Empty")
@Composable
private fun CreateUserEmptyPreview() = AppTheme { CreateUserScreen(CreateUserState()) }

@Preview(showBackground = true, name = "Create - Validation Errors")
@Composable
private fun CreateUserErrorsPreview() = AppTheme {
    CreateUserScreen(CreateUserState(nameError = "Name is required", emailError = "Invalid email format"))
}

@Preview(showBackground = true, name = "Create - Loading")
@Composable
private fun CreateUserLoadingPreview() = AppTheme {
    CreateUserScreen(CreateUserState(name = "Alice", email = "alice@example.com", isLoading = true))
}
```

## DI Wiring

```kotlin
// di/AppModule.kt
@Module
@ComponentScan("com.example.myapp")
class AppModule

// di/NetworkModule.kt
@Module
@ComponentScan("com.example.myapp")
class NetworkModule {
    @Single fun provideHttpClient(): HttpClient = HttpClient(Android) { /* see ktorfit-network.md */ }
    @Single fun provideKtorfit(client: HttpClient): Ktorfit = Ktorfit.Builder().baseUrl("https://jsonplaceholder.typicode.com/").httpClient(client).build()
    @Single fun provideUserApi(ktorfit: Ktorfit): UserApi = ktorfit.create()
}

// MyApp.kt
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApp)
            modules(AppModule().module, NetworkModule().module)
        }
    }
}
```
