package com.soccerprediction.league

import com.soccerprediction.apifootball.ApiFootballLeague
import com.soccerprediction.apifootball.ApiFootballService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.*

class AdminLeagueControllerTest {

    private val leagueService = mockk<LeagueService>()
    private val apiFootballService = mockk<ApiFootballService>()
    private val controller = AdminLeagueController(leagueService, apiFootballService)

    @Test
    fun `createLeague returns 201 with league dto`() {
        val request = CreateLeagueRequest(
            name = "Premier League",
            apiLeagueId = 39,
            season = 2026
        )
        val league = League(
            name = "Premier League",
            apiLeagueId = 39,
            season = 2026,
            joinCode = "ABC12345"
        )

        every { leagueService.createLeague(request) } returns league
        every { leagueService.getMemberCount(league.id) } returns 0L

        val response = controller.createLeague(request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("Premier League", response.body?.name)
        assertEquals("ABC12345", response.body?.joinCode)
        assertEquals(0L, response.body?.memberCount)
    }

    @Test
    fun `updateLeague returns updated league`() {
        val id = UUID.randomUUID()
        val league = League(
            id = id,
            name = "Updated Name",
            apiLeagueId = 39,
            season = 2026,
            joinCode = "ABC12345"
        )

        every { leagueService.updateLeague(id, any()) } returns league
        every { leagueService.getMemberCount(id) } returns 3L

        val response = controller.updateLeague(id, UpdateLeagueRequest(name = "Updated Name"))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Updated Name", response.body?.name)
        assertEquals(3L, response.body?.memberCount)
    }

    @Test
    fun `getAllLeagues returns list`() {
        val league = League(name = "PL", apiLeagueId = 39, season = 2026, joinCode = "XYZ")
        every { leagueService.getAllLeagues() } returns listOf(league)
        every { leagueService.getMemberCount(league.id) } returns 2L

        val response = controller.getAllLeagues()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
    }

    @Test
    fun `searchApiFootballLeagues returns results`() {
        val leagues = listOf(
            ApiFootballLeague(39, "Premier League", "England", null, listOf(2025, 2026))
        )
        every { apiFootballService.searchLeagues("Premier") } returns leagues

        val response = controller.searchApiFootballLeagues("Premier")

        assertEquals(HttpStatus.OK, response.statusCode)
    }
}
