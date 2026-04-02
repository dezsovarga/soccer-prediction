package com.soccerprediction.user

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `findByEmail returns user when exists`() {
        val user = User(
            email = "test@example.com",
            displayName = "Test User"
        )
        userRepository.save(user)

        val found = userRepository.findByEmail("test@example.com")

        assertNotNull(found)
        assertEquals("test@example.com", found?.email)
        assertEquals("Test User", found?.displayName)
    }

    @Test
    fun `findByEmail returns null when not found`() {
        val found = userRepository.findByEmail("nonexistent@example.com")

        assertNull(found)
    }
}
