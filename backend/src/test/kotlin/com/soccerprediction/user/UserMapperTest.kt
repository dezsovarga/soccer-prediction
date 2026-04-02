package com.soccerprediction.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class UserMapperTest {

    @Test
    fun `toDto maps all fields correctly`() {
        val id = UUID.randomUUID()
        val user = User(
            id = id,
            email = "test@example.com",
            displayName = "Test User",
            pictureUrl = "https://example.com/photo.jpg",
            role = UserRole.USER,
            isActive = true,
            createdAt = Instant.now()
        )

        val dto = user.toDto()

        assertEquals(id, dto.id)
        assertEquals("test@example.com", dto.email)
        assertEquals("Test User", dto.displayName)
        assertEquals("https://example.com/photo.jpg", dto.pictureUrl)
        assertEquals("USER", dto.role)
        assertEquals(true, dto.isActive)
    }

    @Test
    fun `toDto maps admin role correctly`() {
        val user = User(
            email = "admin@example.com",
            displayName = "Admin",
            role = UserRole.ADMIN
        )

        val dto = user.toDto()

        assertEquals("ADMIN", dto.role)
    }

    @Test
    fun `toDto handles null pictureUrl`() {
        val user = User(
            email = "test@example.com",
            displayName = "Test",
            pictureUrl = null
        )

        val dto = user.toDto()

        assertEquals(null, dto.pictureUrl)
    }
}
