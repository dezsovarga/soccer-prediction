package com.soccerprediction.user

fun User.toDto() = UserDto(
    id = id,
    email = email,
    displayName = displayName,
    pictureUrl = pictureUrl,
    role = role.name,
    isActive = isActive
)
