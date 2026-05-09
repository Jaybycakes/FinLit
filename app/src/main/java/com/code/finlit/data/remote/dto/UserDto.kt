package com.code.finlit.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
)
