package com.soccerprediction.standing

import com.soccerprediction.fixture.Fixture
import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.league.League
import com.soccerprediction.league.LeagueMode
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.team.Team
import com.soccerprediction.team.TeamRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class StandingComputeServiceTest {
    private val standingRepository = mockk<StandingRepository>()
    private val fixtureRepository = mockk<FixtureRepository>()
    private val teamRepository = mockk<TeamRepository>()
    private val leagueRepository = mockk<LeagueRepository>()

    private val service = StandingComputeService(
        standingRepository, fixtureRepository, teamRepository, leagueRepository
    )

    private val league = League(
        name = "World Cup 2026",
        mode = LeagueMode.MANUAL,
        season = 2026,
        joinCode = "WC262026"
    )

    private val brazil = Team(league = league, name = "Brazil", countryCode = "br", groupName = "A")
    private val germany = Team(league = league, name = "Germany", countryCode = "de", groupName = "A")
    private val spain = Team(league = league, name = "Spain", countryCode = "es", groupName = "A")

    @Test
    fun `recomputeStandings computes correct standings from results`() {
        val fixtures = listOf(
            Fixture(league = league, homeTeam = "Brazil", awayTeam = "Germany",
                homeTeamRef = brazil, awayTeamRef = germany,
                homeScore = 2, awayScore = 1, status = "FINISHED",
                kickoff = Instant.now(), matchday = 1),
            Fixture(league = league, homeTeam = "Brazil", awayTeam = "Spain",
                homeTeamRef = brazil, awayTeamRef = spain,
                homeScore = 1, awayScore = 1, status = "FINISHED",
                kickoff = Instant.now(), matchday = 2),
            Fixture(league = league, homeTeam = "Germany", awayTeam = "Spain",
                homeTeamRef = germany, awayTeamRef = spain,
                homeScore = 0, awayScore = 3, status = "FINISHED",
                kickoff = Instant.now(), matchday = 3)
        )

        every { leagueRepository.findById(league.id) } returns Optional.of(league)
        every { teamRepository.findByLeagueId(league.id) } returns listOf(brazil, germany, spain)
        every { fixtureRepository.findByLeagueIdAndStatus(league.id, "FINISHED") } returns fixtures
        every { standingRepository.deleteByLeagueId(league.id) } just runs

        val savedStandings = mutableListOf<Standing>()
        every { standingRepository.save(any()) } answers {
            val s = firstArg<Standing>()
            savedStandings.add(s)
            s
        }

        service.recomputeStandings(league.id)

        assertEquals(3, savedStandings.size)

        // Brazil: W1 D1 = 4pts, GF3 GA2, GD+1
        val brazilStanding = savedStandings.find { it.teamName == "Brazil" }!!
        assertEquals(4, brazilStanding.points)
        assertEquals(1, brazilStanding.won)
        assertEquals(1, brazilStanding.drawn)
        assertEquals(0, brazilStanding.lost)
        assertEquals(3, brazilStanding.goalsFor)
        assertEquals(2, brazilStanding.goalsAgainst)
        // Spain has better GD (+3 vs +1), so Spain=rank1, Brazil=rank2
        assertEquals(2, brazilStanding.rank)

        // Spain: W1 D1 = 4pts, GF4 GA1, GD+3
        val spainStanding = savedStandings.find { it.teamName == "Spain" }!!
        assertEquals(4, spainStanding.points)
        assertEquals(1, spainStanding.won)
        assertEquals(1, spainStanding.drawn)
        assertEquals(0, spainStanding.lost)
        assertEquals(1, spainStanding.rank)

        // Germany: L2 = 0pts
        val germanyStanding = savedStandings.find { it.teamName == "Germany" }!!
        assertEquals(0, germanyStanding.points)
        assertEquals(0, germanyStanding.won)
        assertEquals(0, germanyStanding.drawn)
        assertEquals(2, germanyStanding.lost)
        assertEquals(3, germanyStanding.rank)
    }

    @Test
    fun `recomputeStandings handles no finished fixtures`() {
        every { leagueRepository.findById(league.id) } returns Optional.of(league)
        every { teamRepository.findByLeagueId(league.id) } returns listOf(brazil, germany)
        every { fixtureRepository.findByLeagueIdAndStatus(league.id, "FINISHED") } returns emptyList()
        every { standingRepository.deleteByLeagueId(league.id) } just runs

        val savedStandings = mutableListOf<Standing>()
        every { standingRepository.save(any()) } answers {
            savedStandings.add(firstArg())
            firstArg()
        }

        service.recomputeStandings(league.id)

        assertEquals(2, savedStandings.size)
        assertTrue(savedStandings.all { it.points == 0 && it.played == 0 })
    }
}
