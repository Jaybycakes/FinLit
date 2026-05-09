package com.code.finlit.data.repository

import com.code.finlit.data.remote.UserApi
import com.code.finlit.data.remote.dto.CreateUserRequest
import com.code.finlit.data.remote.mapper.toDomain
import com.code.finlit.data.remote.mapFailure
import com.code.finlit.data.remote.toNetworkError
import com.code.finlit.domain.model.User
import com.code.finlit.domain.repository.UserRepository
import org.koin.core.annotation.Single

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
