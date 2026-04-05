package com.soccerprediction.prediction

import com.soccerprediction.fixture.Fixture
import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.league.League
import com.soccerprediction.league.LeagueMember
import com.soccerprediction.league.LeagueMemberRepository
import com.soccerprediction.league.LeagueRepository
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
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PredictionIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var leagueRepository: LeagueRepository
    @Autowired private lateinit var leagueMemberRepository: LeagueMemberRepository
    @Autowired private lateinit var fixtureRepository: FixtureRepository
    @Autowired private lateinit var predictionRepository: PredictionRepository
    @Autowired private lateinit var topScorerPickRepository: TopScorerPickRepository
    @Autowired private lateinit var leagueWinnerPickRepository: LeagueWinnerPickRepository
    @Autowired private lateinit var objectMapper: ObjectMapper

    private lateinit var user: User
    private lateinit var league: League
    private lateinit var futureFixture: Fixture
    private lateinit var pastFixture: Fixture

    private fun userOAuth() = oauth2Login().attributes { it["email"] = "user@test.com" }

    @BeforeEach
    fun setup() {
        leagueWinnerPickRepository.deleteAll()
        topScorerPickRepository.deleteAll()
        predictionRepository.deleteAll()
        fixtureRepository.deleteAll()
        leagueMemberRepository.deleteAll()
        leagueRepository.deleteAll()
        userRepository.deleteAll()

        user = userRepository.save(User(email = "user@test.com", displayName = "User", role = UserRole.USER))
        league = leagueRepository.save(League(name = "PL", apiLeagueId = 39, season = 2026, joinCode = "PRED1234"))
        leagueMemberRepository.save(LeagueMember(league = league, user = user))

        futureFixture = fixtureRepository.save(Fixture(
            league = league, apiFixtureId = 100, homeTeam = "Arsenal", awayTeam = "Chelsea",
            kickoff = Instant.now().plusSeconds(86400), matchday = 1
        ))
        pastFixture = fixtureRepository.save(Fixture(
            league = league, apiFixtureId = 101, homeTeam = "Liverpool", awayTeam = "Man City",
            kickoff = Instant.now().minusSeconds(3600), matchday = 1,
            status = "FINISHED", homeScore = 2, awayScore = 1
        ))
    }

    @Test
    fun `PUT predictions creates prediction for future fixture`() {
        mockMvc.perform(
            put("/api/predictions/${futureFixture.id}")
                .with(userOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PredictionRequest(2, 1)))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.homeScore").value(2))
            .andExpect(jsonPath("$.awayScore").value(1))
            .andExpect(jsonPath("$.fixtureHomeTeam").value("Arsenal"))
    }

    @Test
    fun `PUT predictions rejects prediction for past fixture`() {
        mockMvc.perform(
            put("/api/predictions/${pastFixture.id}")
                .with(userOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PredictionRequest(1, 0)))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `PUT predictions updates existing prediction`() {
        predictionRepository.save(Prediction(
            user = user, fixture = futureFixture, homeScore = 1, awayScore = 0
        ))

        mockMvc.perform(
            put("/api/predictions/${futureFixture.id}")
                .with(userOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PredictionRequest(3, 2)))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.homeScore").value(3))
            .andExpect(jsonPath("$.awayScore").value(2))
    }

    @Test
    fun `GET my predictions returns predictions with fixture details`() {
        predictionRepository.save(Prediction(
            user = user, fixture = futureFixture, homeScore = 2, awayScore = 0
        ))

        mockMvc.perform(get("/api/leagues/${league.id}/predictions/me").with(userOAuth()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].homeScore").value(2))
            .andExpect(jsonPath("$[0].fixtureHomeTeam").value("Arsenal"))
    }

    @Test
    fun `unauthenticated prediction requests return 401`() {
        mockMvc.perform(
            put("/api/predictions/${futureFixture.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PredictionRequest(1, 0)))
        )
            .andExpect(status().isUnauthorized)
    }
}
