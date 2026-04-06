package com.soccerprediction.fixture

import com.soccerprediction.league.League
import com.soccerprediction.league.LeagueMode
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.prediction.PredictionService
import com.soccerprediction.standing.StandingComputeService
import com.soccerprediction.team.Team
import com.soccerprediction.team.TeamRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class AdminFixtureServiceTest {
    private val fixtureRepository = mockk<FixtureRepository>()
    private val leagueRepository = mockk<LeagueRepository>()
    private val teamRepository = mockk<TeamRepository>()
    private val predictionService = mockk<PredictionService>()
    private val standingComputeService = mockk<StandingComputeService>()

    private val service = AdminFixtureService(
        fixtureRepository, leagueRepository, teamRepository, predictionService, standingComputeService
    )

    private val league = League(
        name = "World Cup 2026",
        mode = LeagueMode.MANUAL,
        season = 2026,
        joinCode = "WC262026"
    )

    private val brazil = Team(league = league, name = "Brazil", countryCode = "br", groupName = "A")
    private val germany = Team(league = league, name = "Germany", countryCode = "de", groupName = "A")

    @Test
    fun `createFixture creates fixture for manual league`() {
        every { leagueRepository.findById(league.id) } returns Optional.of(league)
        every { teamRepository.findById(brazil.id) } returns Optional.of(brazil)
        every { teamRepository.findById(germany.id) } returns Optional.of(germany)
        every { fixtureRepository.save(any()) } answers { firstArg() }

        val result = service.createFixture(
            league.id,
            CreateFixtureRequest(
                homeTeamId = brazil.id,
                awayTeamId = germany.id,
                kickoff = Instant.parse("2026-06-14T18:00:00Z"),
                round = "Group A",
                matchday = 1
            )
        )

        assertEquals("Brazil", result.homeTeam)
        assertEquals("Germany", result.awayTeam)
        assertEquals("Group A", result.round)
        assertEquals(1, result.matchday)
        assertNotNull(result.homeTeamLogo)
        assertNotNull(result.awayTeamLogo)
    }

    @Test
    fun `createFixture fails for API_SYNCED league`() {
        val apiLeague = League(name = "PL", mode = LeagueMode.API_SYNCED, apiLeagueId = 39, season = 2026, joinCode = "PL123456")
        every { leagueRepository.findById(apiLeague.id) } returns Optional.of(apiLeague)

        assertThrows(IllegalArgumentException::class.java) {
            service.createFixture(apiLeague.id, CreateFixtureRequest(brazil.id, germany.id, Instant.now(), matchday = 1))
        }
    }

    @Test
    fun `enterResult sets scores and triggers scoring`() {
        val fixture = Fixture(
            league = league,
            homeTeam = "Brazil",
            awayTeam = "Germany",
            homeTeamRef = brazil,
            awayTeamRef = germany,
            kickoff = Instant.parse("2026-06-14T18:00:00Z"),
            matchday = 1
        )
        every { fixtureRepository.findById(fixture.id) } returns Optional.of(fixture)
        every { fixtureRepository.save(any()) } answers { firstArg() }
        every { predictionService.calculatePoints(fixture.id) } just runs
        every { standingComputeService.recomputeStandings(league.id) } just runs

        val result = service.enterResult(fixture.id, EnterResultRequest(2, 1))

        assertEquals(2, result.homeScore)
        assertEquals(1, result.awayScore)
        assertEquals("FINISHED", result.status)
        verify { predictionService.calculatePoints(fixture.id) }
        verify { standingComputeService.recomputeStandings(league.id) }
    }

    @Test
    fun `updateFixture updates kickoff and round`() {
        val fixture = Fixture(
            league = league,
            homeTeam = "Brazil",
            awayTeam = "Germany",
            kickoff = Instant.parse("2026-06-14T18:00:00Z"),
            matchday = 1
        )
        every { fixtureRepository.findById(fixture.id) } returns Optional.of(fixture)
        every { fixtureRepository.save(any()) } answers { firstArg() }

        val newKickoff = Instant.parse("2026-06-15T20:00:00Z")
        val result = service.updateFixture(fixture.id, UpdateFixtureRequest(kickoff = newKickoff, round = "Group A"))

        assertEquals(newKickoff, result.kickoff)
        assertEquals("Group A", result.round)
    }

    @Test
    fun `deleteFixture removes fixture`() {
        val fixture = Fixture(
            league = league,
            homeTeam = "Brazil",
            awayTeam = "Germany",
            kickoff = Instant.now(),
            matchday = 1
        )
        every { fixtureRepository.findById(fixture.id) } returns Optional.of(fixture)
        every { fixtureRepository.delete(fixture) } just runs

        assertDoesNotThrow { service.deleteFixture(fixture.id) }
        verify { fixtureRepository.delete(fixture) }
    }
}
