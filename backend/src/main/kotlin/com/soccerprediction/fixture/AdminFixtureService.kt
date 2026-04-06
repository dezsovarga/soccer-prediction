package com.soccerprediction.fixture

import com.soccerprediction.league.LeagueMode
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.prediction.PredictionService
import com.soccerprediction.standing.StandingComputeService
import com.soccerprediction.team.TeamRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AdminFixtureService(
    private val fixtureRepository: FixtureRepository,
    private val leagueRepository: LeagueRepository,
    private val teamRepository: TeamRepository,
    private val predictionService: PredictionService,
    private val standingComputeService: StandingComputeService
) {
    @Transactional
    fun createFixture(leagueId: UUID, request: CreateFixtureRequest): Fixture {
        val league = leagueRepository.findById(leagueId)
            .orElseThrow { IllegalArgumentException("League not found") }
        require(league.mode == LeagueMode.MANUAL) { "Fixtures can only be created in manual leagues" }

        val homeTeam = teamRepository.findById(request.homeTeamId)
            .orElseThrow { IllegalArgumentException("Home team not found") }
        val awayTeam = teamRepository.findById(request.awayTeamId)
            .orElseThrow { IllegalArgumentException("Away team not found") }

        val homeLogoUrl = homeTeam.logoUrl ?: homeTeam.countryCode?.let {
            "https://flagcdn.com/w80/${it.lowercase()}.png"
        }
        val awayLogoUrl = awayTeam.logoUrl ?: awayTeam.countryCode?.let {
            "https://flagcdn.com/w80/${it.lowercase()}.png"
        }

        return fixtureRepository.save(
            Fixture(
                league = league,
                homeTeam = homeTeam.name,
                awayTeam = awayTeam.name,
                homeTeamLogo = homeLogoUrl,
                awayTeamLogo = awayLogoUrl,
                homeTeamRef = homeTeam,
                awayTeamRef = awayTeam,
                kickoff = request.kickoff,
                round = request.round,
                matchday = request.matchday
            )
        )
    }

    @Transactional
    fun updateFixture(fixtureId: UUID, request: UpdateFixtureRequest): Fixture {
        val fixture = fixtureRepository.findById(fixtureId)
            .orElseThrow { IllegalArgumentException("Fixture not found") }

        request.homeTeamId?.let { id ->
            val team = teamRepository.findById(id)
                .orElseThrow { IllegalArgumentException("Home team not found") }
            fixture.homeTeamRef = team
            fixture.homeTeam = team.name
            fixture.homeTeamLogo = team.logoUrl ?: team.countryCode?.let {
                "https://flagcdn.com/w80/${it.lowercase()}.png"
            }
        }
        request.awayTeamId?.let { id ->
            val team = teamRepository.findById(id)
                .orElseThrow { IllegalArgumentException("Away team not found") }
            fixture.awayTeamRef = team
            fixture.awayTeam = team.name
            fixture.awayTeamLogo = team.logoUrl ?: team.countryCode?.let {
                "https://flagcdn.com/w80/${it.lowercase()}.png"
            }
        }
        request.kickoff?.let { fixture.kickoff = it }
        request.round?.let { fixture.round = it }
        request.matchday?.let { fixture.matchday = it }
        fixture.updatedAt = Instant.now()

        return fixtureRepository.save(fixture)
    }

    @Transactional
    fun deleteFixture(fixtureId: UUID) {
        val fixture = fixtureRepository.findById(fixtureId)
            .orElseThrow { IllegalArgumentException("Fixture not found") }
        fixtureRepository.delete(fixture)
    }

    @Transactional
    fun enterResult(fixtureId: UUID, request: EnterResultRequest): Fixture {
        val fixture = fixtureRepository.findById(fixtureId)
            .orElseThrow { IllegalArgumentException("Fixture not found") }

        fixture.homeScore = request.homeScore
        fixture.awayScore = request.awayScore
        fixture.status = "FINISHED"
        fixture.updatedAt = Instant.now()

        val saved = fixtureRepository.save(fixture)

        predictionService.calculatePoints(saved.id)
        standingComputeService.recomputeStandings(saved.league.id)

        return saved
    }

    fun getFixtures(leagueId: UUID): List<Fixture> {
        return fixtureRepository.findByLeagueIdOrderByKickoff(leagueId)
    }
}
