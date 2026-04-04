package com.soccerprediction.league

import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.standing.StandingRepository
import com.soccerprediction.user.User
import com.soccerprediction.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.*

class LeagueControllerTest {

    private val leagueService = mockk<LeagueService>()
    private val leagueMemberRepository = mockk<LeagueMemberRepository>()
    private val leagueRepository = mockk<LeagueRepository>()
    private val fixtureRepository = mockk<FixtureRepository>()
    private val standingRepository = mockk<StandingRepository>()
    private val userRepository = mockk<UserRepository>()

    private val controller = LeagueController(
        leagueService, leagueMemberRepository, leagueRepository,
        fixtureRepository, standingRepository, userRepository
    )

    private val testUser = User(email = "test@example.com", displayName = "Test User")
    private val testLeague = League(name = "PL", apiLeagueId = 39, season = 2026, joinCode = "ABC12345")

    private fun mockOAuth2User(email: String?): OAuth2User {
        val oauth2User = mockk<OAuth2User>()
        every { oauth2User.getAttribute<String>("email") } returns email
        return oauth2User
    }

    @Test
    fun `getMyLeagues returns leagues for authenticated user`() {
        every { userRepository.findByEmail("test@example.com") } returns testUser
        val member = LeagueMember(league = testLeague, user = testUser)
        every { leagueMemberRepository.findByUserId(testUser.id) } returns listOf(member)
        every { leagueService.getMemberCount(testLeague.id) } returns 3L

        val response = controller.getMyLeagues(mockOAuth2User("test@example.com"))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        assertEquals("PL", response.body?.first()?.name)
    }

    @Test
    fun `getMyLeagues returns 401 when user not found`() {
        every { userRepository.findByEmail("unknown@example.com") } returns null

        val response = controller.getMyLeagues(mockOAuth2User("unknown@example.com"))

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `joinLeague creates membership and returns 201`() {
        every { userRepository.findByEmail("test@example.com") } returns testUser
        every { leagueRepository.findByJoinCode("ABC12345") } returns testLeague
        every { leagueMemberRepository.findByLeagueIdAndUserId(testLeague.id, testUser.id) } returns null
        every { leagueMemberRepository.save(any()) } answers { firstArg() }
        every { leagueService.getMemberCount(testLeague.id) } returns 1L

        val response = controller.joinLeague(
            mockOAuth2User("test@example.com"),
            JoinLeagueRequest("ABC12345")
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("PL", response.body?.name)
    }

    @Test
    fun `joinLeague returns 200 when already a member`() {
        every { userRepository.findByEmail("test@example.com") } returns testUser
        every { leagueRepository.findByJoinCode("ABC12345") } returns testLeague
        val member = LeagueMember(league = testLeague, user = testUser)
        every { leagueMemberRepository.findByLeagueIdAndUserId(testLeague.id, testUser.id) } returns member
        every { leagueService.getMemberCount(testLeague.id) } returns 1L

        val response = controller.joinLeague(
            mockOAuth2User("test@example.com"),
            JoinLeagueRequest("ABC12345")
        )

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `joinLeague returns 404 for invalid code`() {
        every { userRepository.findByEmail("test@example.com") } returns testUser
        every { leagueRepository.findByJoinCode("INVALID") } returns null

        val response = controller.joinLeague(
            mockOAuth2User("test@example.com"),
            JoinLeagueRequest("INVALID")
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `getLeague returns 403 when not a member`() {
        every { userRepository.findByEmail("test@example.com") } returns testUser
        every { leagueMemberRepository.findByLeagueIdAndUserId(testLeague.id, testUser.id) } returns null

        val response = controller.getLeague(mockOAuth2User("test@example.com"), testLeague.id)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `getLeague returns league detail when member`() {
        every { userRepository.findByEmail("test@example.com") } returns testUser
        val member = LeagueMember(league = testLeague, user = testUser)
        every { leagueMemberRepository.findByLeagueIdAndUserId(testLeague.id, testUser.id) } returns member
        every { leagueService.getMemberCount(testLeague.id) } returns 2L

        val response = controller.getLeague(mockOAuth2User("test@example.com"), testLeague.id)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("PL", response.body?.name)
    }

    @Test
    fun `getFixtures returns fixtures for league member`() {
        every { userRepository.findByEmail("test@example.com") } returns testUser
        val member = LeagueMember(league = testLeague, user = testUser)
        every { leagueMemberRepository.findByLeagueIdAndUserId(testLeague.id, testUser.id) } returns member
        every { fixtureRepository.findByLeagueIdOrderByKickoff(testLeague.id) } returns emptyList()

        val response = controller.getFixtures(mockOAuth2User("test@example.com"), testLeague.id)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(0, response.body?.size)
    }

    @Test
    fun `getStandings returns standings for league member`() {
        every { userRepository.findByEmail("test@example.com") } returns testUser
        val member = LeagueMember(league = testLeague, user = testUser)
        every { leagueMemberRepository.findByLeagueIdAndUserId(testLeague.id, testUser.id) } returns member
        every { standingRepository.findByLeagueIdOrderByRank(testLeague.id) } returns emptyList()

        val response = controller.getStandings(mockOAuth2User("test@example.com"), testLeague.id)

        assertEquals(HttpStatus.OK, response.statusCode)
    }
}
