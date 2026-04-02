package com.soccerprediction.user

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.user.OAuth2User

class UserControllerTest {

    private val userRepository = mockk<UserRepository>()
    private val controller = UserController(userRepository)

    private fun mockOAuth2User(email: String?): OAuth2User {
        val oauth2User = mockk<OAuth2User>()
        every { oauth2User.getAttribute<String>("email") } returns email
        return oauth2User
    }

    @Test
    fun `getCurrentUser returns user dto when user exists`() {
        val user = User(
            email = "test@example.com",
            displayName = "Test User",
            pictureUrl = "https://example.com/photo.jpg"
        )
        every { userRepository.findByEmail("test@example.com") } returns user

        val response = controller.getCurrentUser(mockOAuth2User("test@example.com"))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("test@example.com", response.body?.email)
        assertEquals("Test User", response.body?.displayName)
    }

    @Test
    fun `getCurrentUser returns 404 when user not found`() {
        every { userRepository.findByEmail("unknown@example.com") } returns null

        val response = controller.getCurrentUser(mockOAuth2User("unknown@example.com"))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `getCurrentUser returns 400 when email is null`() {
        val response = controller.getCurrentUser(mockOAuth2User(null))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}
