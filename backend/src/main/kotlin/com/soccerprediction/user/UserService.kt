package com.soccerprediction.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getAllUsers(): List<AdminUserDto> {
        return userRepository.findAll().map { it.toAdminDto() }
    }

    @Transactional
    fun updateUser(id: UUID, request: UpdateUserRequest): AdminUserDto {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found") }

        request.isActive?.let { user.isActive = it }

        return userRepository.save(user).toAdminDto()
    }
}
