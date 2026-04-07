package com.soccerprediction.user

import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.league.LeagueMemberRepository
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.prediction.LeagueWinnerPickRepository
import com.soccerprediction.prediction.PredictionRepository
import com.soccerprediction.prediction.TopScorerPickRepository
import com.soccerprediction.standing.StandingRepository
import com.soccerprediction.team.TeamRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var leagueRepository: LeagueRepository
    @Autowired private lateinit var leagueMemberRepository: LeagueMemberRepository
    @Autowired private lateinit var fixtureRepository: FixtureRepository
    @Autowired private lateinit var predictionRepository: PredictionRepository
    @Autowired private lateinit var topScorerPickRepository: TopScorerPickRepository
    @Autowired private lateinit var leagueWinnerPickRepository: LeagueWinnerPickRepository
    @Autowired private lateinit var standingRepository: StandingRepository
    @Autowired private lateinit var teamRepository: TeamRepository
    @Autowired private lateinit var objectMapper: ObjectMapper

    private lateinit var adminUser: User
    private lateinit var regularUser: User

    private fun adminOAuth() = oauth2Login().attributes { it["email"] = "admin@test.com" }
        .authorities(SimpleGrantedAuthority("ROLE_ADMIN"))

    private fun userOAuth() = oauth2Login().attributes { it["email"] = "user@test.com" }

    @BeforeEach
    fun setup() {
        leagueWinnerPickRepository.deleteAll()
        topScorerPickRepository.deleteAll()
        predictionRepository.deleteAll()
        standingRepository.deleteAll()
        fixtureRepository.deleteAll()
        teamRepository.deleteAll()
        leagueMemberRepository.deleteAll()
        leagueRepository.deleteAll()
        userRepository.deleteAll()

        adminUser = userRepository.save(
            User(email = "admin@test.com", displayName = "Admin", role = UserRole.ADMIN)
        )
        regularUser = userRepository.save(
            User(email = "user@test.com", displayName = "User", role = UserRole.USER)
        )
    }

    @Test
    fun `GET admin users returns all users`() {
        mockMvc.perform(get("/api/admin/users").with(adminOAuth()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].email").exists())
            .andExpect(jsonPath("$[0].createdAt").exists())
    }

    @Test
    fun `PATCH admin users toggles isActive`() {
        mockMvc.perform(
            patch("/api/admin/users/${regularUser.id}")
                .with(adminOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateUserRequest(isActive = false)))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isActive").value(false))
            .andExpect(jsonPath("$.email").value("user@test.com"))
    }

    @Test
    fun `PATCH admin users returns 404 for non-existent user`() {
        mockMvc.perform(
            patch("/api/admin/users/${UUID.randomUUID()}")
                .with(adminOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateUserRequest(isActive = false)))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET admin users returns 403 for non-admin`() {
        mockMvc.perform(get("/api/admin/users").with(userOAuth()))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `GET admin users returns 401 for unauthenticated`() {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized)
    }
}
