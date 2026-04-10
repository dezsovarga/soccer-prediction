package com.soccerprediction.team

import com.soccerprediction.league.League
import com.soccerprediction.league.LeagueMode
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.standing.StandingComputeService
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class TeamServiceTest {
    private val teamRepository = mockk<TeamRepository>()
    private val leagueRepository = mockk<LeagueRepository>()
    private val standingComputeService = mockk<StandingComputeService>(relaxed = true)
    private val service = TeamService(teamRepository, leagueRepository, standingComputeService)

    private val manualLeague = League(
        name = "World Cup 2026",
        mode = LeagueMode.MANUAL,
        season = 2026,
        joinCode = "WC262026"
    )

    private val apiLeague = League(
        name = "Premier League",
        mode = LeagueMode.API_SYNCED,
        apiLeagueId = 39,
        season = 2026,
        joinCode = "PL262026"
    )

    @Test
    fun `createTeam succeeds for manual league`() {
        every { leagueRepository.findById(manualLeague.id) } returns Optional.of(manualLeague)
        every { teamRepository.save(any()) } answers { firstArg() }

        val result = service.createTeam(manualLeague.id, CreateTeamRequest("Brazil", "br", "A"))

        assertEquals("Brazil", result.name)
        assertEquals("br", result.countryCode)
        assertEquals("https://flagcdn.com/w80/br.png", result.logoUrl)
        assertEquals("A", result.groupName)
    }

    @Test
    fun `createTeam fails for API_SYNCED league`() {
        every { leagueRepository.findById(apiLeague.id) } returns Optional.of(apiLeague)

        assertThrows(IllegalArgumentException::class.java) {
            service.createTeam(apiLeague.id, CreateTeamRequest("Arsenal"))
        }
    }

    @Test
    fun `updateTeam updates fields`() {
        val team = Team(league = manualLeague, name = "Brasil", countryCode = "br")
        every { teamRepository.findById(team.id) } returns Optional.of(team)
        every { teamRepository.save(any()) } answers { firstArg() }

        val result = service.updateTeam(team.id, UpdateTeamRequest(name = "Brazil"))

        assertEquals("Brazil", result.name)
    }

    @Test
    fun `deleteTeam removes team`() {
        val team = Team(league = manualLeague, name = "Brazil", countryCode = "br")
        every { teamRepository.findById(team.id) } returns Optional.of(team)
        every { teamRepository.delete(team) } just runs

        assertDoesNotThrow { service.deleteTeam(team.id) }
        verify { teamRepository.delete(team) }
    }

    @Test
    fun `getTeams returns teams for league`() {
        val teams = listOf(
            Team(league = manualLeague, name = "Brazil", countryCode = "br", groupName = "A"),
            Team(league = manualLeague, name = "Germany", countryCode = "de", groupName = "A")
        )
        every { teamRepository.findByLeagueIdOrderByGroupNameAscNameAsc(manualLeague.id) } returns teams

        val result = service.getTeams(manualLeague.id)

        assertEquals(2, result.size)
    }
}
