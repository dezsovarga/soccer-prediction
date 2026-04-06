package com.soccerprediction.fixture

import com.soccerprediction.league.League
import com.soccerprediction.league.LeagueMemberRepository
import com.soccerprediction.league.LeagueMode
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.prediction.LeagueWinnerPickRepository
import com.soccerprediction.prediction.PredictionRepository
import com.soccerprediction.prediction.TopScorerPickRepository
import com.soccerprediction.standing.StandingRepository
import com.soccerprediction.team.Team
import com.soccerprediction.team.TeamRepository
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
class AdminFixtureIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var leagueRepository: LeagueRepository
    @Autowired private lateinit var leagueMemberRepository: LeagueMemberRepository
    @Autowired private lateinit var teamRepository: TeamRepository
    @Autowired private lateinit var fixtureRepository: FixtureRepository
    @Autowired private lateinit var predictionRepository: PredictionRepository
    @Autowired private lateinit var topScorerPickRepository: TopScorerPickRepository
    @Autowired private lateinit var leagueWinnerPickRepository: LeagueWinnerPickRepository
    @Autowired private lateinit var standingRepository: StandingRepository
    @Autowired private lateinit var objectMapper: ObjectMapper

    private lateinit var adminUser: User
    private lateinit var league: League
    private lateinit var brazil: Team
    private lateinit var germany: Team

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
        league = leagueRepository.save(
            League(name = "World Cup 2026", mode = LeagueMode.MANUAL, season = 2026, joinCode = "WC262026")
        )
        brazil = teamRepository.save(Team(league = league, name = "Brazil", countryCode = "br", groupName = "A"))
        germany = teamRepository.save(Team(league = league, name = "Germany", countryCode = "de", groupName = "A"))
    }

    private fun adminOAuth() = oauth2Login().attributes { it["email"] = "admin@test.com" }
        .authorities(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))

    @Test
    fun `POST creates fixture`() {
        val request = CreateFixtureRequest(
            homeTeamId = brazil.id,
            awayTeamId = germany.id,
            kickoff = Instant.parse("2026-06-14T18:00:00Z"),
            round = "Group A",
            matchday = 1
        )

        mockMvc.perform(
            post("/api/admin/leagues/${league.id}/fixtures")
                .with(adminOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.homeTeam").value("Brazil"))
            .andExpect(jsonPath("$.awayTeam").value("Germany"))
            .andExpect(jsonPath("$.round").value("Group A"))
            .andExpect(jsonPath("$.matchday").value(1))
            .andExpect(jsonPath("$.status").value("SCHEDULED"))
    }

    @Test
    fun `PUT result enters score and triggers standings`() {
        val fixture = fixtureRepository.save(
            Fixture(
                league = league,
                homeTeam = "Brazil",
                awayTeam = "Germany",
                homeTeamRef = brazil,
                awayTeamRef = germany,
                kickoff = Instant.parse("2026-06-14T18:00:00Z"),
                round = "Group A",
                matchday = 1
            )
        )

        mockMvc.perform(
            put("/api/admin/fixtures/${fixture.id}/result")
                .with(adminOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(EnterResultRequest(2, 1)))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.homeScore").value(2))
            .andExpect(jsonPath("$.awayScore").value(1))
            .andExpect(jsonPath("$.status").value("FINISHED"))

        // Verify standings were computed
        val standings = standingRepository.findByLeagueIdOrderByRank(league.id)
        assert(standings.isNotEmpty())
    }

    @Test
    fun `DELETE removes fixture`() {
        val fixture = fixtureRepository.save(
            Fixture(
                league = league,
                homeTeam = "Brazil",
                awayTeam = "Germany",
                kickoff = Instant.now(),
                matchday = 1
            )
        )

        mockMvc.perform(delete("/api/admin/fixtures/${fixture.id}").with(adminOAuth()))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `GET returns fixtures for league`() {
        fixtureRepository.save(
            Fixture(
                league = league,
                homeTeam = "Brazil",
                awayTeam = "Germany",
                kickoff = Instant.now(),
                matchday = 1
            )
        )

        mockMvc.perform(get("/api/admin/leagues/${league.id}/fixtures").with(adminOAuth()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
    }
}
