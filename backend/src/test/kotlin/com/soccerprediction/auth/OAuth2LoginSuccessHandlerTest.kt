package com.soccerprediction.auth

import com.soccerprediction.user.User
import com.soccerprediction.user.UserRepository
import com.soccerprediction.user.UserRole
import io.mockk.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User

class OAuth2LoginSuccessHandlerTest {

    private val userRepository = mockk<UserRepository>()
    private val request = mockk<HttpServletRequest>()
    private val response = mockk<HttpServletResponse>(relaxed = true)
    private val authentication = mockk<Authentication>()
    private val oauth2User = mockk<OAuth2User>()

    private val handler = OAuth2LoginSuccessHandler(
        userRepository = userRepository,
        adminEmail = "admin@test.com",
        frontendUrl = "http://localhost:5173"
    )

    @BeforeEach
    fun setup() {
        every { authentication.principal } returns oauth2User
        every { oauth2User.getAttribute<String>("email") } returns "user@test.com"
        every { oauth2User.getAttribute<String>("name") } returns "Test User"
        every { oauth2User.getAttribute<String>("picture") } returns "https://example.com/photo.jpg"
    }

    @Test
    fun `creates new user with USER role on first login`() {
        every { userRepository.findByEmail("user@test.com") } returns null
        every { userRepository.save(any()) } answers { firstArg() }

        handler.onAuthenticationSuccess(request, response, authentication)

        verify {
            userRepository.save(match { user: User ->
                user.email == "user@test.com" &&
                user.displayName == "Test User" &&
                user.role == UserRole.USER
            })
        }
    }

    @Test
    fun `creates new user with ADMIN role when email matches admin email`() {
        every { oauth2User.getAttribute<String>("email") } returns "admin@test.com"
        every { userRepository.findByEmail("admin@test.com") } returns null
        every { userRepository.save(any()) } answers { firstArg() }

        handler.onAuthenticationSuccess(request, response, authentication)

        verify {
            userRepository.save(match { user: User ->
                user.email == "admin@test.com" &&
                user.role == UserRole.ADMIN
            })
        }
    }

    @Test
    fun `updates existing user display name and picture`() {
        val existingUser = User(
            email = "user@test.com",
            displayName = "Old Name",
            pictureUrl = "https://example.com/old.jpg"
        )
        every { userRepository.findByEmail("user@test.com") } returns existingUser
        every { userRepository.save(any()) } answers { firstArg() }

        handler.onAuthenticationSuccess(request, response, authentication)

        assertEquals("Test User", existingUser.displayName)
        assertEquals("https://example.com/photo.jpg", existingUser.pictureUrl)
        verify { userRepository.save(existingUser) }
    }

    @Test
    fun `redirects to frontend url after success`() {
        every { userRepository.findByEmail("user@test.com") } returns null
        every { userRepository.save(any()) } answers { firstArg() }

        handler.onAuthenticationSuccess(request, response, authentication)

        verify { response.sendRedirect("http://localhost:5173") }
    }
}
