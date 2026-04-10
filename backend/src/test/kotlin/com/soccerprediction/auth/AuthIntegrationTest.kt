package com.soccerprediction.auth

import com.soccerprediction.league.LeagueMemberRepository
import com.soccerprediction.user.User
import com.soccerprediction.user.UserRepository
import com.soccerprediction.user.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var leagueMemberRepository: LeagueMemberRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        leagueMemberRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `GET api users me returns 401 when not authenticated`() {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET api admin endpoint returns 401 when not authenticated`() {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET api users me returns user when authenticated`() {
        val user = userRepository.save(
            User(
                email = "test@example.com",
                displayName = "Test User",
                pictureUrl = "https://example.com/photo.jpg",
                role = UserRole.USER
            )
        )

        mockMvc.perform(
            get("/api/users/me")
                .with(oauth2Login().attributes { attrs ->
                    attrs["email"] = "test@example.com"
                    attrs["name"] = "Test User"
                    attrs["picture"] = "https://example.com/photo.jpg"
                })
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.displayName").value("Test User"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.id").value(user.id.toString()))
    }

    @Test
    fun `GET api users me returns 404 when user not in database`() {
        mockMvc.perform(
            get("/api/users/me")
                .with(oauth2Login().attributes { attrs ->
                    attrs["email"] = "notindb@example.com"
                })
        )
            .andExpect(status().isNotFound)
    }
}
