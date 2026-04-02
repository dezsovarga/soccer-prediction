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
