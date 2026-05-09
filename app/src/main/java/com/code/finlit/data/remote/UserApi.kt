package com.code.finlit.data.remote

import com.code.finlit.data.remote.dto.CreateUserRequest
import com.code.finlit.data.remote.dto.UserDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path

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
