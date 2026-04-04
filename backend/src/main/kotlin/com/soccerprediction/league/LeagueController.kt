package com.soccerprediction.league

import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.fixture.toDto
import com.soccerprediction.fixture.FixtureDto
import com.soccerprediction.standing.StandingRepository
import com.soccerprediction.standing.toDto
import com.soccerprediction.standing.StandingDto
import com.soccerprediction.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/leagues")
class LeagueController(
    private val leagueService: LeagueService,
    private val leagueMemberRepository: LeagueMemberRepository,
    private val leagueRepository: LeagueRepository,
    private val fixtureRepository: FixtureRepository,
    private val standingRepository: StandingRepository,
    private val userRepository: UserRepository
) {
    @GetMapping
    fun getMyLeagues(@AuthenticationPrincipal oauth2User: OAuth2User): ResponseEntity<List<LeagueSummaryDto>> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val memberships = leagueMemberRepository.findByUserId(user.id)
        val dtos = memberships.map {
            it.league.toSummaryDto(leagueService.getMemberCount(it.league.id))
        }
        return ResponseEntity.ok(dtos)
    }

    @PostMapping("/join")
    fun joinLeague(
        @AuthenticationPrincipal oauth2User: OAuth2User,
        @RequestBody request: JoinLeagueRequest
    ): ResponseEntity<LeagueSummaryDto> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val league = leagueRepository.findByJoinCode(request.joinCode)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        val existing = leagueMemberRepository.findByLeagueIdAndUserId(league.id, user.id)
        if (existing != null) {
            return ResponseEntity.ok(league.toSummaryDto(leagueService.getMemberCount(league.id)))
        }

        leagueMemberRepository.save(LeagueMember(league = league, user = user))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(league.toSummaryDto(leagueService.getMemberCount(league.id)))
    }

    @GetMapping("/{id}")
    fun getLeague(
        @AuthenticationPrincipal oauth2User: OAuth2User,
        @PathVariable id: UUID
    ): ResponseEntity<LeagueDto> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val membership = leagueMemberRepository.findByLeagueIdAndUserId(id, user.id)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        val league = membership.league
        return ResponseEntity.ok(league.toDto(leagueService.getMemberCount(league.id)))
    }

    @GetMapping("/{id}/fixtures")
    fun getFixtures(
        @AuthenticationPrincipal oauth2User: OAuth2User,
        @PathVariable id: UUID
    ): ResponseEntity<List<FixtureDto>> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        leagueMemberRepository.findByLeagueIdAndUserId(id, user.id)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        val fixtures = fixtureRepository.findByLeagueIdOrderByKickoff(id).map { it.toDto() }
        return ResponseEntity.ok(fixtures)
    }

    @GetMapping("/{id}/standings")
    fun getStandings(
        @AuthenticationPrincipal oauth2User: OAuth2User,
        @PathVariable id: UUID
    ): ResponseEntity<List<StandingDto>> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        leagueMemberRepository.findByLeagueIdAndUserId(id, user.id)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        val standings = standingRepository.findByLeagueIdOrderByRank(id).map { it.toDto() }
        return ResponseEntity.ok(standings)
    }

    private fun getUser(oauth2User: OAuth2User) =
        oauth2User.getAttribute<String>("email")?.let { userRepository.findByEmail(it) }
}
