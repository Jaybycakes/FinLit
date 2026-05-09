package com.code.finlit.data.remote.mapper

import com.code.finlit.data.remote.dto.UserDto
import com.code.finlit.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
)
