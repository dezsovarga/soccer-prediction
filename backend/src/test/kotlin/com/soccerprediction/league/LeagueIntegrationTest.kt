package com.soccerprediction.league

import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.prediction.LeagueWinnerPickRepository
import com.soccerprediction.prediction.PredictionRepository
import com.soccerprediction.prediction.TopScorerPickRepository
import com.soccerprediction.user.User
import com.soccerprediction.user.UserRepository
import com.soccerprediction.user.UserRole
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeagueIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var leagueRepository: LeagueRepository

    @Autowired
    private lateinit var leagueMemberRepository: LeagueMemberRepository

    @Autowired
    private lateinit var fixtureRepository: FixtureRepository

    @Autowired
    private lateinit var predictionRepository: PredictionRepository

    @Autowired
    private lateinit var topScorerPickRepository: TopScorerPickRepository

    @Autowired
    private lateinit var leagueWinnerPickRepository: LeagueWinnerPickRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var adminUser: User
    private lateinit var regularUser: User

    @BeforeEach
    fun setup() {
        leagueWinnerPickRepository.deleteAll()
        topScorerPickRepository.deleteAll()
        predictionRepository.deleteAll()
        fixtureRepository.deleteAll()
        leagueMemberRepository.deleteAll()
        leagueRepository.deleteAll()
        userRepository.deleteAll()

        adminUser = userRepository.save(
            User(email = "admin@test.com", displayName = "Admin", role = UserRole.ADMIN)
        )
        regularUser = userRepository.save(
            User(email = "user@test.com", displayName = "Regular User", role = UserRole.USER)
        )
    }

    private fun adminOAuth() = oauth2Login().attributes { it["email"] = "admin@test.com" }
        .authorities(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))

    private fun userOAuth() = oauth2Login().attributes { it["email"] = "user@test.com" }

    @Test
    fun `POST api admin leagues creates league when admin`() {
        val request = CreateLeagueRequest(name = "Test League", apiLeagueId = 39, season = 2026)

        mockMvc.perform(
            post("/api/admin/leagues")
                .with(adminOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Test League"))
            .andExpect(jsonPath("$.apiLeagueId").value(39))
            .andExpect(jsonPath("$.season").value(2026))
            .andExpect(jsonPath("$.joinCode").isNotEmpty)
    }

    @Test
    fun `POST api admin leagues returns 403 for non-admin`() {
        val request = CreateLeagueRequest(name = "Test", apiLeagueId = 39, season = 2026)

        mockMvc.perform(
            post("/api/admin/leagues")
                .with(userOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `PUT api admin leagues id updates league`() {
        val league = leagueRepository.save(
            League(name = "Old", apiLeagueId = 39, season = 2026, joinCode = "TEST1234")
        )

        mockMvc.perform(
            put("/api/admin/leagues/${league.id}")
                .with(adminOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateLeagueRequest(name = "Updated")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))
    }

    @Test
    fun `POST api leagues join with valid code creates membership`() {
        val league = leagueRepository.save(
            League(name = "PL", apiLeagueId = 39, season = 2026, joinCode = "JOIN1234")
        )

        mockMvc.perform(
            post("/api/leagues/join")
                .with(userOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(JoinLeagueRequest("JOIN1234")))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("PL"))
    }

    @Test
    fun `POST api leagues join with invalid code returns 404`() {
        mockMvc.perform(
            post("/api/leagues/join")
                .with(userOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(JoinLeagueRequest("INVALID1")))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET api leagues returns only user's leagues`() {
        val league = leagueRepository.save(
            League(name = "My League", apiLeagueId = 39, season = 2026, joinCode = "MINE1234")
        )
        leagueMemberRepository.save(LeagueMember(league = league, user = regularUser))

        val otherLeague = leagueRepository.save(
            League(name = "Other", apiLeagueId = 140, season = 2026, joinCode = "OTHER123")
        )

        mockMvc.perform(get("/api/leagues").with(userOAuth()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("My League"))
    }

    @Test
    fun `GET api leagues id returns 403 when not a member`() {
        val league = leagueRepository.save(
            League(name = "Secret", apiLeagueId = 39, season = 2026, joinCode = "SECR1234")
        )

        mockMvc.perform(get("/api/leagues/${league.id}").with(userOAuth()))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `GET api leagues id returns league detail when member`() {
        val league = leagueRepository.save(
            League(name = "My League", apiLeagueId = 39, season = 2026, joinCode = "DETA1234")
        )
        leagueMemberRepository.save(LeagueMember(league = league, user = regularUser))

        mockMvc.perform(get("/api/leagues/${league.id}").with(userOAuth()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("My League"))
            .andExpect(jsonPath("$.joinCode").value("DETA1234"))
    }

    @Test
    fun `GET api leagues id fixtures returns empty for new league`() {
        val league = leagueRepository.save(
            League(name = "New", apiLeagueId = 39, season = 2026, joinCode = "FIXT1234")
        )
        leagueMemberRepository.save(LeagueMember(league = league, user = regularUser))

        mockMvc.perform(get("/api/leagues/${league.id}/fixtures").with(userOAuth()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `GET api leagues id standings returns empty for new league`() {
        val league = leagueRepository.save(
            League(name = "New", apiLeagueId = 39, season = 2026, joinCode = "STND1234")
        )
        leagueMemberRepository.save(LeagueMember(league = league, user = regularUser))

        mockMvc.perform(get("/api/leagues/${league.id}/standings").with(userOAuth()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `unauthenticated requests return 401`() {
        mockMvc.perform(get("/api/leagues"))
            .andExpect(status().isUnauthorized)

        mockMvc.perform(post("/api/leagues/join")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"joinCode\":\"ABC\"}"))
            .andExpect(status().isUnauthorized)
    }
}
