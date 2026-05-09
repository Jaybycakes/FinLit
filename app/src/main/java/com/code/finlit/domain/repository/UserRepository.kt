package com.code.finlit.domain.repository

import com.code.finlit.domain.model.User

interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUser(id: Int): Result<User>
    suspend fun createUser(name: String, email: String): Result<User>
    suspend fun deleteUser(id: Int): Result<Unit>
}
