package com.soccerprediction.leaderboard

import com.soccerprediction.fixture.Fixture
import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.league.League
import com.soccerprediction.league.LeagueMember
import com.soccerprediction.league.LeagueMemberRepository
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.prediction.*
import com.soccerprediction.standing.StandingRepository
import com.soccerprediction.team.TeamRepository
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
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeaderboardIntegrationTest {

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

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var league: League

    private fun userOAuth() = oauth2Login().attributes { it["email"] = "alice@test.com" }

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

        user1 = userRepository.save(User(email = "alice@test.com", displayName = "Alice", role = UserRole.USER))
        user2 = userRepository.save(User(email = "bob@test.com", displayName = "Bob", role = UserRole.USER))
        league = leagueRepository.save(League(name = "WC 2026", season = 2026, joinCode = "LB1234"))
        leagueMemberRepository.save(LeagueMember(league = league, user = user1))
        leagueMemberRepository.save(LeagueMember(league = league, user = user2))
    }

    @Test
    fun `GET leaderboard returns ranked entries`() {
        val fixture = fixtureRepository.save(Fixture(
            league = league, homeTeam = "Brazil", awayTeam = "Germany",
            kickoff = Instant.now().minusSeconds(7200), matchday = 1,
            status = "FINISHED", homeScore = 2, awayScore = 1
        ))

        predictionRepository.save(Prediction(
            user = user1, fixture = fixture, homeScore = 2, awayScore = 1
        ).also { it.pointsEarned = 3 })
        predictionRepository.save(Prediction(
            user = user2, fixture = fixture, homeScore = 3, awayScore = 0
        ).also { it.pointsEarned = 1 })

        mockMvc.perform(get("/api/leagues/${league.id}/leaderboard").with(userOAuth()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].displayName").value("Alice"))
            .andExpect(jsonPath("$[0].totalPoints").value(3))
            .andExpect(jsonPath("$[0].rank").value(1))
            .andExpect(jsonPath("$[0].correctScores").value(1))
            .andExpect(jsonPath("$[1].displayName").value("Bob"))
            .andExpect(jsonPath("$[1].totalPoints").value(1))
            .andExpect(jsonPath("$[1].rank").value(2))
            .andExpect(jsonPath("$[1].correctOutcomes").value(1))
    }

    @Test
    fun `GET leaderboard includes bonus points`() {
        topScorerPickRepository.save(TopScorerPick(
            user = user1, league = league, playerName = "Mbappe"
        ).also { it.pointsEarned = 10 })

        mockMvc.perform(get("/api/leagues/${league.id}/leaderboard").with(userOAuth()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].displayName").value("Alice"))
            .andExpect(jsonPath("$[0].totalPoints").value(10))
            .andExpect(jsonPath("$[0].topScorerPoints").value(10))
    }

    @Test
    fun `GET leaderboard returns 404 for non-existent league`() {
        mockMvc.perform(get("/api/leagues/${UUID.randomUUID()}/leaderboard").with(userOAuth()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET leaderboard returns 401 for unauthenticated user`() {
        mockMvc.perform(get("/api/leagues/${league.id}/leaderboard"))
            .andExpect(status().isUnauthorized)
    }
}
