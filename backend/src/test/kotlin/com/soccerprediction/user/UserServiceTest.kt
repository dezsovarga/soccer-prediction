package com.soccerprediction.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UserServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val service = UserService(userRepository)

    private val user1 = User(email = "alice@test.com", displayName = "Alice", role = UserRole.USER)
    private val user2 = User(email = "bob@test.com", displayName = "Bob", role = UserRole.ADMIN)

    @Test
    fun `getAllUsers returns all users as AdminUserDto`() {
        every { userRepository.findAll() } returns listOf(user1, user2)

        val result = service.getAllUsers()

        assertEquals(2, result.size)
        assertEquals("alice@test.com", result[0].email)
        assertEquals("USER", result[0].role)
        assertEquals("bob@test.com", result[1].email)
        assertEquals("ADMIN", result[1].role)
    }

    @Test
    fun `updateUser toggles isActive`() {
        every { userRepository.findById(user1.id) } returns Optional.of(user1)
        every { userRepository.save(any()) } answers { firstArg() }

        val result = service.updateUser(user1.id, UpdateUserRequest(isActive = false))

        assertFalse(result.isActive)
        verify { userRepository.save(user1) }
    }

    @Test
    fun `updateUser throws for non-existent user`() {
        val fakeId = UUID.randomUUID()
        every { userRepository.findById(fakeId) } returns Optional.empty()

        assertThrows<IllegalArgumentException> {
            service.updateUser(fakeId, UpdateUserRequest(isActive = false))
        }
    }

    @Test
    fun `updateUser with null isActive does not change status`() {
        every { userRepository.findById(user1.id) } returns Optional.of(user1)
        every { userRepository.save(any()) } answers { firstArg() }

        val result = service.updateUser(user1.id, UpdateUserRequest())

        assertTrue(result.isActive) // default is true
    }
}
