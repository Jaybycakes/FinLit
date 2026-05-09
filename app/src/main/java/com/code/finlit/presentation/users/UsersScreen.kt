package com.code.finlit.presentation.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.code.finlit.domain.model.User
import com.code.finlit.ui.theme.FinLitTheme
import org.koin.androidx.compose.koinViewModel

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

@OptIn(ExperimentalMaterial3Api::class)
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
        Text("No users found", style = MaterialTheme.typography.bodyLarge)
    }
}

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

@Preview(showBackground = true, name = "Users - Loading")
@Composable
private fun UsersLoadingPreview() {
    FinLitTheme {
        UsersScreen(state = UsersState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "Users - List")
@Composable
private fun UsersListPreview() {
    FinLitTheme {
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

@Preview(showBackground = true, name = "Users - Error")
@Composable
private fun UsersErrorPreview() {
    FinLitTheme {
        UsersScreen(state = UsersState(error = "No internet connection"))
    }
}

@Preview(showBackground = true, name = "Users - Empty")
@Composable
private fun UsersEmptyPreview() {
    FinLitTheme {
        UsersScreen(state = UsersState())
    }
}

@Preview(showBackground = true)
@Composable
private fun UserCardPreview() {
    FinLitTheme {
        UserCard(
            user = User(1, "Alice Smith", "alice@example.com"),
            onClick = {},
            onDeleteClick = {},
        )
    }
}
