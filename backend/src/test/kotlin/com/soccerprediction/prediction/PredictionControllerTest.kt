package com.soccerprediction.prediction

import com.soccerprediction.fixture.Fixture
import com.soccerprediction.league.League
import com.soccerprediction.user.User
import com.soccerprediction.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.Instant
import java.util.UUID

class PredictionControllerTest {

    private val predictionService = mockk<PredictionService>()
    private val pickService = mockk<PickService>()
    private val userRepository = mockk<UserRepository>()

    private val controller = PredictionController(predictionService, pickService, userRepository)

    private val user = User(email = "test@test.com", displayName = "Test")
    private val league = League(name = "PL", apiLeagueId = 39, season = 2026, joinCode = "TEST1234")

    private fun oauth2User(email: String = "test@test.com") = DefaultOAuth2User(
        listOf(SimpleGrantedAuthority("USER")),
        mapOf("email" to email, "sub" to "123"),
        "sub"
    )

    @Test
    fun `PUT predictions returns 200 on success`() {
        val fixture = Fixture(
            league = league, apiFixtureId = 1, homeTeam = "A", awayTeam = "B",
            kickoff = Instant.now().plusSeconds(3600), matchday = 1
        )
        val prediction = Prediction(user = user, fixture = fixture, homeScore = 2, awayScore = 1)

        every { userRepository.findByEmail("test@test.com") } returns user
        every { predictionService.createOrUpdatePrediction(user, fixture.id, any()) } returns prediction

        val response = controller.createOrUpdatePrediction(
            oauth2User(), fixture.id, PredictionRequest(2, 1)
        )

        assertEquals(200, response.statusCode.value())
        assertEquals(2, response.body!!.homeScore)
    }

    @Test
    fun `PUT predictions returns 400 after kickoff`() {
        every { userRepository.findByEmail("test@test.com") } returns user
        every { predictionService.createOrUpdatePrediction(user, any(), any()) } throws
            IllegalStateException("Cannot predict after kickoff")

        val response = controller.createOrUpdatePrediction(
            oauth2User(), UUID.randomUUID(), PredictionRequest(1, 0)
        )

        assertEquals(400, response.statusCode.value())
    }

    @Test
    fun `PUT predictions returns 403 for non-member`() {
        every { userRepository.findByEmail("test@test.com") } returns user
        every { predictionService.createOrUpdatePrediction(user, any(), any()) } throws
            IllegalAccessException("Not a member")

        val response = controller.createOrUpdatePrediction(
            oauth2User(), UUID.randomUUID(), PredictionRequest(1, 0)
        )

        assertEquals(403, response.statusCode.value())
    }

    @Test
    fun `GET my predictions returns list`() {
        val fixture = Fixture(
            league = league, apiFixtureId = 1, homeTeam = "A", awayTeam = "B",
            kickoff = Instant.now(), matchday = 1, status = "FINISHED", homeScore = 2, awayScore = 1
        )
        val prediction = Prediction(
            user = user, fixture = fixture, homeScore = 2, awayScore = 1, pointsEarned = 3
        )

        every { userRepository.findByEmail("test@test.com") } returns user
        every { predictionService.getUserPredictions(user.id, league.id) } returns listOf(prediction)

        val response = controller.getMyPredictions(oauth2User(), league.id)

        assertEquals(200, response.statusCode.value())
        assertEquals(1, response.body!!.size)
        assertEquals(3, response.body!![0].pointsEarned)
    }

    @Test
    fun `GET top scorer pick returns 204 when no pick`() {
        every { userRepository.findByEmail("test@test.com") } returns user
        every { pickService.getTopScorerPick(user.id, league.id) } returns null

        val response = controller.getTopScorerPick(oauth2User(), league.id)

        assertEquals(204, response.statusCode.value())
    }

    @Test
    fun `PUT top scorer pick returns 200 on success`() {
        val pick = TopScorerPick(
            user = user, league = league, playerName = "Haaland", apiPlayerId = 123
        )

        every { userRepository.findByEmail("test@test.com") } returns user
        every { pickService.setTopScorerPick(user, league.id, any()) } returns pick

        val response = controller.setTopScorerPick(
            oauth2User(), league.id, TopScorerPickRequest("Haaland", 123)
        )

        assertEquals(200, response.statusCode.value())
        assertEquals("Haaland", response.body!!.playerName)
    }

    @Test
    fun `PUT league winner pick returns 400 when locked`() {
        every { userRepository.findByEmail("test@test.com") } returns user
        every { pickService.setLeagueWinnerPick(user, any(), any()) } throws
            IllegalStateException("Locked")

        val response = controller.setLeagueWinnerPick(
            oauth2User(), league.id, LeagueWinnerPickRequest("Arsenal", 42)
        )

        assertEquals(400, response.statusCode.value())
    }
}
