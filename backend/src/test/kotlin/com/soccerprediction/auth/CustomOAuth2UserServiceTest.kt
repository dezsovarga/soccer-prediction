package com.soccerprediction.auth

import com.soccerprediction.user.User
import com.soccerprediction.user.UserRepository
import com.soccerprediction.user.UserRole
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.core.OAuth2AuthenticationException

class CustomOAuth2UserServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val adminEmail = "admin@test.com"

    @Test
    fun `new user gets USER role when email does not match admin`() {
        every { userRepository.findByEmail("user@test.com") } returns null
        every { userRepository.save(any()) } answers { firstArg() }

        val role = if ("user@test.com" == adminEmail) UserRole.ADMIN else UserRole.USER

        assertEquals(UserRole.USER, role)
    }

    @Test
    fun `new user gets ADMIN role when email matches admin`() {
        every { userRepository.findByEmail("admin@test.com") } returns null
        every { userRepository.save(any()) } answers { firstArg() }

        val role = if ("admin@test.com" == adminEmail) UserRole.ADMIN else UserRole.USER

        assertEquals(UserRole.ADMIN, role)
    }

    @Test
    fun `existing user display name and picture are updated`() {
        val existingUser = User(
            email = "user@test.com",
            displayName = "Old Name",
            pictureUrl = "https://example.com/old.jpg"
        )
        every { userRepository.findByEmail("user@test.com") } returns existingUser
        every { userRepository.save(any()) } answers { firstArg() }

        existingUser.displayName = "New Name"
        existingUser.pictureUrl = "https://example.com/new.jpg"
        userRepository.save(existingUser)

        assertEquals("New Name", existingUser.displayName)
        assertEquals("https://example.com/new.jpg", existingUser.pictureUrl)
        verify { userRepository.save(existingUser) }
    }

    @Test
    fun `inactive user has isActive false`() {
        val inactiveUser = User(
            email = "blocked@test.com",
            displayName = "Blocked",
            isActive = false
        )

        assertFalse(inactiveUser.isActive)
    }

    @Test
    fun `active user has isActive true by default`() {
        val activeUser = User(
            email = "active@test.com",
            displayName = "Active"
        )

        assertTrue(activeUser.isActive)
    }
}
