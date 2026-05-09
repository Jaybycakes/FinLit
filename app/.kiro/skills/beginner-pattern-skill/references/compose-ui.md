# Compose UI

## Screen Structure (Stateful wrapper + Stateless content)

```kotlin
// presentation/users/UsersScreen.kt

// Stateful — owns ViewModel, handles side effects
@Composable
fun UsersRoute(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: UsersViewModel = koinViewModel(),
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Side effects
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel) {
        viewModel.container.sideEffectFlow
            .flowWithLifecycle(lifecycle)
            .collect { effect ->
                when (effect) {
                    is UsersSideEffect.ShowMessage ->
                        snackbarHostState.showSnackbar(effect.message)
                    is UsersSideEffect.NavigateToDetail ->
                        onNavigateToDetail(effect.userId)
                }
            }
    }

    UsersScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}

// Stateless — pure function of state, easy to Preview
@Composable
fun UsersScreen(
    state: UsersState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (UsersIntent) -> Unit = {},
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Users") })
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
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
```

## Reusable Content Composables

```kotlin
@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun EmptyContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No items found", style = MaterialTheme.typography.bodyLarge)
    }
}
```

## List

```kotlin
@Composable
fun UsersList(
    users: List<User>,
    onUserClick: (User) -> Unit,
    onDeleteClick: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(users, key = { it.id }) { user ->
            UserCard(
                user = user,
                onClick = { onUserClick(user) },
                onDeleteClick = { onDeleteClick(user) },
            )
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete ${user.name}")
            }
        }
    }
}
```

## Previews — mandatory on every Composable

```kotlin
@Preview(showBackground = true, name = "Loading")
@Composable
private fun UsersScreenLoadingPreview() {
    AppTheme {
        UsersScreen(state = UsersState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
private fun UsersScreenSuccessPreview() {
    AppTheme {
        UsersScreen(
            state = UsersState(
                users = listOf(
                    User(1, "Alice", "alice@example.com"),
                    User(2, "Bob", "bob@example.com"),
                ),
            ),
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun UsersScreenErrorPreview() {
    AppTheme {
        UsersScreen(state = UsersState(error = "No internet connection"))
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun UsersScreenEmptyPreview() {
    AppTheme {
        UsersScreen(state = UsersState(users = emptyList()))
    }
}

// Card preview
@Preview(showBackground = true)
@Composable
private fun UserCardPreview() {
    AppTheme {
        UserCard(
            user = User(1, "Alice Smith", "alice@example.com"),
            onClick = {},
            onDeleteClick = {},
        )
    }
}
```

## Theme Setup

```kotlin
// ui/theme/Theme.kt
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

## collectAsStateWithLifecycle — always use this

```kotlin
// CORRECT
val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

// WRONG — doesn't pause on background, wastes battery
val state by viewModel.container.stateFlow.collectAsState()
```

Requires: `lifecycle-runtime-compose` dependency.
