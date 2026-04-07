package com.soccerprediction.user

import java.util.UUID

data class UserDto(
    val id: UUID,
    val email: String,
    val displayName: String,
    val pictureUrl: String?,
    val role: String,
    val isActive: Boolean
)

data class AdminUserDto(
    val id: UUID,
    val email: String,
    val displayName: String,
    val pictureUrl: String?,
    val role: String,
    val isActive: Boolean,
    val createdAt: java.time.Instant
)

data class UpdateUserRequest(
    val isActive: Boolean? = null
)
