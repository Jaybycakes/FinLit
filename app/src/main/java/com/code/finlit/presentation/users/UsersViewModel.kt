package com.code.finlit.presentation.users

import androidx.lifecycle.ViewModel
import com.code.finlit.domain.error.NetworkError
import com.code.finlit.domain.repository.UserRepository
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class UsersViewModel(
    private val repository: UserRepository,
) : ViewModel(), ContainerHost<UsersState, UsersSideEffect> {

    override val container = container<UsersState, UsersSideEffect>(UsersState())

    init {
        onIntent(UsersIntent.LoadUsers)
    }

    fun onIntent(intent: UsersIntent) = when (intent) {
        UsersIntent.LoadUsers -> loadUsers()
        is UsersIntent.DeleteUser -> deleteUser(intent.id)
        is UsersIntent.SelectUser -> intent {
            postSideEffect(UsersSideEffect.NavigateToDetail(intent.id))
        }
    }

    private fun loadUsers() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        repository.getUsers()
            .onSuccess { users ->
                reduce { state.copy(isLoading = false, users = users) }
            }
            .onFailure { error ->
                reduce { state.copy(isLoading = false, error = error.toUserMessage()) }
                postSideEffect(UsersSideEffect.ShowMessage(error.toUserMessage()))
            }
    }

    private fun deleteUser(id: Int) = intent {
        repository.deleteUser(id)
            .onSuccess {
                reduce { state.copy(users = state.users.filter { it.id != id }) }
                postSideEffect(UsersSideEffect.ShowMessage("Deleted successfully"))
            }
            .onFailure { error ->
                postSideEffect(UsersSideEffect.ShowMessage(error.toUserMessage()))
            }
    }
}

fun Throwable.toUserMessage(): String = when (this) {
    is NetworkError.NoInternet -> "No internet connection"
    is NetworkError.Timeout -> "Request timed out. Try again."
    is NetworkError.ServerError -> "Server error ($code). Try again later."
    else -> "Something went wrong. Try again."
}
