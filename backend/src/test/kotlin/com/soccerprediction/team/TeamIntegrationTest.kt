package com.soccerprediction.team

import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.league.League
import com.soccerprediction.league.LeagueMemberRepository
import com.soccerprediction.league.LeagueMode
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.prediction.LeagueWinnerPickRepository
import com.soccerprediction.prediction.PredictionRepository
import com.soccerprediction.prediction.TopScorerPickRepository
import com.soccerprediction.standing.StandingRepository
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
class TeamIntegrationTest {

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
    }

    private fun adminOAuth() = oauth2Login().attributes { it["email"] = "admin@test.com" }
        .authorities(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))

    @Test
    fun `POST creates team for manual league`() {
        mockMvc.perform(
            post("/api/admin/leagues/${league.id}/teams")
                .with(adminOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateTeamRequest("Brazil", "br", "A")))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Brazil"))
            .andExpect(jsonPath("$.countryCode").value("br"))
            .andExpect(jsonPath("$.logoUrl").value("https://flagcdn.com/w80/br.png"))
            .andExpect(jsonPath("$.groupName").value("A"))
    }

    @Test
    fun `GET returns teams for league`() {
        teamRepository.save(Team(league = league, name = "Brazil", countryCode = "br", groupName = "A"))
        teamRepository.save(Team(league = league, name = "Germany", countryCode = "de", groupName = "A"))

        mockMvc.perform(get("/api/admin/leagues/${league.id}/teams").with(adminOAuth()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `PUT updates team`() {
        val team = teamRepository.save(
            Team(league = league, name = "Brasil", countryCode = "br", groupName = "A")
        )

        mockMvc.perform(
            put("/api/admin/teams/${team.id}")
                .with(adminOAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateTeamRequest(name = "Brazil")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Brazil"))
    }

    @Test
    fun `DELETE removes team`() {
        val team = teamRepository.save(
            Team(league = league, name = "Brazil", countryCode = "br")
        )

        mockMvc.perform(delete("/api/admin/teams/${team.id}").with(adminOAuth()))
            .andExpect(status().isNoContent)
    }
}
