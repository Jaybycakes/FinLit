package com.code.finlit.presentation.users

import com.code.finlit.domain.model.User

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
