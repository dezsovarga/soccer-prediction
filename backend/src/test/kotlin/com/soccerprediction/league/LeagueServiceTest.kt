package com.soccerprediction.league

import com.soccerprediction.apifootball.SyncService
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class LeagueServiceTest {

    private val leagueRepository = mockk<LeagueRepository>()
    private val leagueMemberRepository = mockk<LeagueMemberRepository>()
    private val syncService = mockk<SyncService>(relaxed = true)
    private val service = LeagueService(leagueRepository, leagueMemberRepository, syncService)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    fun `createLeague creates league and triggers sync`() {
        val request = CreateLeagueRequest(
            name = "Premier League",
            apiLeagueId = 39,
            season = 2026
        )

        every { leagueRepository.save(any()) } answers { firstArg() }

        val league = service.createLeague(request)

        assertEquals("Premier League", league.name)
        assertEquals(39, league.apiLeagueId)
        assertEquals(2026, league.season)
        assertEquals(8, league.joinCode.length)
        assertEquals(3, league.exactScorePoints)

        verify { syncService.syncFixtures(any(), 39, 2026) }
        verify { syncService.syncStandings(any(), 39, 2026) }
        verify { syncService.syncSquad(any(), 39, 2026) }
    }

    @Test
    fun `createLeague succeeds even if sync fails`() {
        val request = CreateLeagueRequest(
            name = "La Liga",
            apiLeagueId = 140,
            season = 2026
        )

        every { leagueRepository.save(any()) } answers { firstArg() }
        every { syncService.syncFixtures(any(), any(), any()) } throws RuntimeException("API down")

        val league = service.createLeague(request)

        assertEquals("La Liga", league.name)
    }

    @Test
    fun `updateLeague updates only provided fields`() {
        val league = League(
            name = "Old Name",
            apiLeagueId = 39,
            season = 2026,
            joinCode = "ABC12345"
        )

        every { leagueRepository.findById(league.id) } returns Optional.of(league)
        every { leagueRepository.save(any()) } answers { firstArg() }

        val updated = service.updateLeague(league.id, UpdateLeagueRequest(name = "New Name"))

        assertEquals("New Name", updated.name)
        assertEquals(3, updated.exactScorePoints)
    }

    @Test
    fun `updateLeague throws when league not found`() {
        val id = UUID.randomUUID()
        every { leagueRepository.findById(id) } returns Optional.empty()

        assertThrows(IllegalArgumentException::class.java) {
            service.updateLeague(id, UpdateLeagueRequest(name = "X"))
        }
    }

    @Test
    fun `getMemberCount delegates to repository`() {
        val id = UUID.randomUUID()
        every { leagueMemberRepository.countByLeagueId(id) } returns 5L

        assertEquals(5L, service.getMemberCount(id))
    }
}
