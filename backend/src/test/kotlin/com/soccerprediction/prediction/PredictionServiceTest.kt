package com.soccerprediction.prediction

import com.soccerprediction.fixture.Fixture
import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.league.League
import com.soccerprediction.league.LeagueMember
import com.soccerprediction.league.LeagueMemberRepository
import com.soccerprediction.user.User
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class PredictionServiceTest {

    private val predictionRepository = mockk<PredictionRepository>()
    private val fixtureRepository = mockk<FixtureRepository>()
    private val leagueMemberRepository = mockk<LeagueMemberRepository>()

    private val service = PredictionService(
        predictionRepository, fixtureRepository, leagueMemberRepository
    )

    private val league = League(name = "PL", apiLeagueId = 39, season = 2026, joinCode = "TEST1234")
    private val user = User(email = "test@test.com", displayName = "Test")

    @Test
    fun `createOrUpdatePrediction creates new prediction`() {
        val fixture = Fixture(
            league = league, apiFixtureId = 1, homeTeam = "A", awayTeam = "B",
            kickoff = Instant.now().plusSeconds(3600), matchday = 1
        )
        every { fixtureRepository.findById(fixture.id) } returns Optional.of(fixture)
        every { leagueMemberRepository.findByLeagueIdAndUserId(league.id, user.id) } returns
            LeagueMember(league = league, user = user)
        every { predictionRepository.findByUserIdAndFixtureId(user.id, fixture.id) } returns null
        every { predictionRepository.save(any()) } answers { firstArg() }

        val result = service.createOrUpdatePrediction(user, fixture.id, PredictionRequest(2, 1))

        assertEquals(2, result.homeScore)
        assertEquals(1, result.awayScore)
        verify { predictionRepository.save(any()) }
    }

    @Test
    fun `createOrUpdatePrediction updates existing prediction`() {
        val fixture = Fixture(
            league = league, apiFixtureId = 1, homeTeam = "A", awayTeam = "B",
            kickoff = Instant.now().plusSeconds(3600), matchday = 1
        )
        val existing = Prediction(user = user, fixture = fixture, homeScore = 1, awayScore = 0)

        every { fixtureRepository.findById(fixture.id) } returns Optional.of(fixture)
        every { leagueMemberRepository.findByLeagueIdAndUserId(league.id, user.id) } returns
            LeagueMember(league = league, user = user)
        every { predictionRepository.findByUserIdAndFixtureId(user.id, fixture.id) } returns existing
        every { predictionRepository.save(any()) } answers { firstArg() }

        val result = service.createOrUpdatePrediction(user, fixture.id, PredictionRequest(3, 2))

        assertEquals(3, result.homeScore)
        assertEquals(2, result.awayScore)
    }

    @Test
    fun `createOrUpdatePrediction rejects prediction after kickoff`() {
        val fixture = Fixture(
            league = league, apiFixtureId = 1, homeTeam = "A", awayTeam = "B",
            kickoff = Instant.now().minusSeconds(60), matchday = 1
        )
        every { fixtureRepository.findById(fixture.id) } returns Optional.of(fixture)
        every { leagueMemberRepository.findByLeagueIdAndUserId(league.id, user.id) } returns
            LeagueMember(league = league, user = user)

        assertThrows<IllegalStateException> {
            service.createOrUpdatePrediction(user, fixture.id, PredictionRequest(1, 0))
        }
    }

    @Test
    fun `createOrUpdatePrediction rejects non-member`() {
        val fixture = Fixture(
            league = league, apiFixtureId = 1, homeTeam = "A", awayTeam = "B",
            kickoff = Instant.now().plusSeconds(3600), matchday = 1
        )
        every { fixtureRepository.findById(fixture.id) } returns Optional.of(fixture)
        every { leagueMemberRepository.findByLeagueIdAndUserId(league.id, user.id) } returns null

        assertThrows<IllegalAccessException> {
            service.createOrUpdatePrediction(user, fixture.id, PredictionRequest(1, 0))
        }
    }

    @Test
    fun `calculatePoints scores exact match`() {
        val points = PredictionService.calculatePointsForPrediction(
            predictedHome = 2, predictedAway = 1,
            actualHome = 2, actualAway = 1,
            exactScorePoints = 3, correctOutcomePoints = 1, wrongPredictionPoints = 0
        )
        assertEquals(3, points)
    }

    @Test
    fun `calculatePoints scores correct outcome`() {
        val points = PredictionService.calculatePointsForPrediction(
            predictedHome = 3, predictedAway = 0,
            actualHome = 2, actualAway = 1,
            exactScorePoints = 3, correctOutcomePoints = 1, wrongPredictionPoints = 0
        )
        assertEquals(1, points)
    }

    @Test
    fun `calculatePoints scores wrong prediction`() {
        val points = PredictionService.calculatePointsForPrediction(
            predictedHome = 2, predictedAway = 0,
            actualHome = 0, actualAway = 1,
            exactScorePoints = 3, correctOutcomePoints = 1, wrongPredictionPoints = 0
        )
        assertEquals(0, points)
    }

    @Test
    fun `calculatePoints handles draw correctly`() {
        val points = PredictionService.calculatePointsForPrediction(
            predictedHome = 1, predictedAway = 1,
            actualHome = 0, actualAway = 0,
            exactScorePoints = 3, correctOutcomePoints = 1, wrongPredictionPoints = 0
        )
        assertEquals(1, points) // correct outcome (draw), wrong score
    }

    @Test
    fun `calculatePoints for fixture updates all predictions`() {
        val fixture = Fixture(
            league = league, apiFixtureId = 1, homeTeam = "A", awayTeam = "B",
            kickoff = Instant.now().minusSeconds(7200), matchday = 1,
            status = "FINISHED", homeScore = 2, awayScore = 1
        )
        val p1 = Prediction(user = user, fixture = fixture, homeScore = 2, awayScore = 1) // exact
        val p2 = Prediction(user = user, fixture = fixture, homeScore = 3, awayScore = 0) // outcome
        val p3 = Prediction(user = user, fixture = fixture, homeScore = 0, awayScore = 2) // wrong

        every { fixtureRepository.findById(fixture.id) } returns Optional.of(fixture)
        every { predictionRepository.findByFixtureId(fixture.id) } returns listOf(p1, p2, p3)
        every { predictionRepository.saveAll(any<List<Prediction>>()) } answers { firstArg() }

        service.calculatePoints(fixture.id)

        assertEquals(3, p1.pointsEarned)
        assertEquals(1, p2.pointsEarned)
        assertEquals(0, p3.pointsEarned)
    }
}
