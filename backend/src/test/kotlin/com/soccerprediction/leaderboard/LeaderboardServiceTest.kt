package com.soccerprediction.leaderboard

import com.soccerprediction.fixture.Fixture
import com.soccerprediction.league.League
import com.soccerprediction.league.LeagueMember
import com.soccerprediction.league.LeagueMemberRepository
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.prediction.LeagueWinnerPick
import com.soccerprediction.prediction.LeagueWinnerPickRepository
import com.soccerprediction.prediction.Prediction
import com.soccerprediction.prediction.PredictionRepository
import com.soccerprediction.prediction.TopScorerPick
import com.soccerprediction.prediction.TopScorerPickRepository
import com.soccerprediction.user.User
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class LeaderboardServiceTest {

    private val leagueRepository = mockk<LeagueRepository>()
    private val leagueMemberRepository = mockk<LeagueMemberRepository>()
    private val predictionRepository = mockk<PredictionRepository>()
    private val topScorerPickRepository = mockk<TopScorerPickRepository>()
    private val leagueWinnerPickRepository = mockk<LeagueWinnerPickRepository>()

    private val service = LeaderboardService(
        leagueRepository, leagueMemberRepository, predictionRepository,
        topScorerPickRepository, leagueWinnerPickRepository
    )

    private val league = League(name = "WC 2026", season = 2026, joinCode = "WC2026")
    private val user1 = User(email = "alice@test.com", displayName = "Alice")
    private val user2 = User(email = "bob@test.com", displayName = "Bob")
    private val user3 = User(email = "carol@test.com", displayName = "Carol")

    private fun fixture(homeTeam: String = "A", awayTeam: String = "B") = Fixture(
        league = league, homeTeam = homeTeam, awayTeam = awayTeam,
        kickoff = Instant.now().minusSeconds(7200), matchday = 1,
        status = "FINISHED", homeScore = 2, awayScore = 1
    )

    private fun prediction(user: User, fixture: Fixture, home: Int, away: Int, points: Int?) =
        Prediction(user = user, fixture = fixture, homeScore = home, awayScore = away).also {
            it.pointsEarned = points
        }

    @Test
    fun `getLeaderboard returns ranked entries sorted by total points`() {
        val f1 = fixture()
        val f2 = fixture("C", "D")

        every { leagueRepository.findById(league.id) } returns Optional.of(league)
        every { leagueMemberRepository.findByLeagueId(league.id) } returns listOf(
            LeagueMember(league = league, user = user1),
            LeagueMember(league = league, user = user2)
        )
        every { predictionRepository.findByFixtureLeagueId(league.id) } returns listOf(
            prediction(user1, f1, 2, 1, 3),  // exact
            prediction(user1, f2, 1, 0, 1),  // outcome
            prediction(user2, f1, 0, 0, 0),  // wrong
            prediction(user2, f2, 2, 1, 3)   // exact
        )
        every { topScorerPickRepository.findByLeagueId(league.id) } returns emptyList()
        every { leagueWinnerPickRepository.findByLeagueId(league.id) } returns emptyList()

        val result = service.getLeaderboard(league.id)

        assertEquals(2, result.size)
        // Alice: 3 + 1 = 4 points
        assertEquals("Alice", result[0].displayName)
        assertEquals(4, result[0].totalPoints)
        assertEquals(1, result[0].rank)
        assertEquals(1, result[0].correctScores)
        assertEquals(1, result[0].correctOutcomes)
        // Bob: 0 + 3 = 3 points
        assertEquals("Bob", result[1].displayName)
        assertEquals(3, result[1].totalPoints)
        assertEquals(2, result[1].rank)
        assertEquals(1, result[1].correctScores)
    }

    @Test
    fun `getLeaderboard includes bonus pick points`() {
        every { leagueRepository.findById(league.id) } returns Optional.of(league)
        every { leagueMemberRepository.findByLeagueId(league.id) } returns listOf(
            LeagueMember(league = league, user = user1)
        )
        every { predictionRepository.findByFixtureLeagueId(league.id) } returns listOf(
            prediction(user1, fixture(), 2, 1, 3)
        )
        every { topScorerPickRepository.findByLeagueId(league.id) } returns listOf(
            TopScorerPick(user = user1, league = league, playerName = "Mbappe").also {
                it.pointsEarned = 10
            }
        )
        every { leagueWinnerPickRepository.findByLeagueId(league.id) } returns listOf(
            LeagueWinnerPick(user = user1, league = league, teamName = "France").also {
                it.pointsEarned = 10
            }
        )

        val result = service.getLeaderboard(league.id)

        assertEquals(1, result.size)
        assertEquals(23, result[0].totalPoints) // 3 + 10 + 10
        assertEquals(10, result[0].topScorerPoints)
        assertEquals(10, result[0].leagueWinnerPoints)
    }

    @Test
    fun `getLeaderboard ranks by points then correct scores then name`() {
        val f1 = fixture()

        every { leagueRepository.findById(league.id) } returns Optional.of(league)
        every { leagueMemberRepository.findByLeagueId(league.id) } returns listOf(
            LeagueMember(league = league, user = user1),
            LeagueMember(league = league, user = user2),
            LeagueMember(league = league, user = user3)
        )
        every { predictionRepository.findByFixtureLeagueId(league.id) } returns listOf(
            prediction(user1, f1, 2, 1, 3),  // exact = 3pts
            prediction(user2, f1, 3, 0, 1),  // outcome = 1pt
            prediction(user3, f1, 3, 2, 1)   // outcome = 1pt
        )
        every { topScorerPickRepository.findByLeagueId(league.id) } returns emptyList()
        every { leagueWinnerPickRepository.findByLeagueId(league.id) } returns emptyList()

        val result = service.getLeaderboard(league.id)

        assertEquals(3, result.size)
        assertEquals("Alice", result[0].displayName) // 3 pts
        assertEquals("Bob", result[1].displayName)   // 1 pt, 0 exact, alphabetical tiebreak
        assertEquals("Carol", result[2].displayName)  // 1 pt, 0 exact, alphabetical tiebreak
    }

    @Test
    fun `getLeaderboard returns empty entries for members with no predictions`() {
        every { leagueRepository.findById(league.id) } returns Optional.of(league)
        every { leagueMemberRepository.findByLeagueId(league.id) } returns listOf(
            LeagueMember(league = league, user = user1)
        )
        every { predictionRepository.findByFixtureLeagueId(league.id) } returns emptyList()
        every { topScorerPickRepository.findByLeagueId(league.id) } returns emptyList()
        every { leagueWinnerPickRepository.findByLeagueId(league.id) } returns emptyList()

        val result = service.getLeaderboard(league.id)

        assertEquals(1, result.size)
        assertEquals(0, result[0].totalPoints)
        assertEquals(0, result[0].correctScores)
        assertEquals(0, result[0].correctOutcomes)
        assertEquals(1, result[0].rank)
    }

    @Test
    fun `getLeaderboard throws for non-existent league`() {
        val fakeId = UUID.randomUUID()
        every { leagueRepository.findById(fakeId) } returns Optional.empty()

        assertThrows<IllegalArgumentException> {
            service.getLeaderboard(fakeId)
        }
    }
}
