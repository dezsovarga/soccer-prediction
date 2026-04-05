package com.soccerprediction.apifootball

import com.soccerprediction.fixture.Fixture
import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.player.Player
import com.soccerprediction.player.PlayerRepository
import com.soccerprediction.prediction.PredictionService
import com.soccerprediction.standing.Standing
import com.soccerprediction.standing.StandingRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SyncService(
    private val apiFootballService: ApiFootballService,
    private val leagueRepository: LeagueRepository,
    private val fixtureRepository: FixtureRepository,
    private val standingRepository: StandingRepository,
    private val playerRepository: PlayerRepository,
    private val predictionService: PredictionService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 4 * * *")
    fun syncAll() {
        val leagues = leagueRepository.findAll()
        for (league in leagues) {
            try {
                syncFixtures(league.id.toString(), league.apiLeagueId, league.season)
                syncStandings(league.id.toString(), league.apiLeagueId, league.season)
            } catch (e: Exception) {
                log.error("Failed to sync league ${league.name}: ${e.message}")
            }
        }
    }

    fun syncFixtures(leagueIdStr: String, apiLeagueId: Int, season: Int) {
        val leagueId = java.util.UUID.fromString(leagueIdStr)
        val league = leagueRepository.findById(leagueId).orElse(null) ?: return

        val apiFixtures = apiFootballService.getFixtures(apiLeagueId, season)
        log.info("Syncing ${apiFixtures.size} fixtures for league ${league.name}")

        for (af in apiFixtures) {
            val existing = fixtureRepository.findByApiFixtureId(af.fixtureId)
            if (existing != null) {
                val wasNotFinished = existing.status != "FINISHED"
                existing.homeTeam = af.homeTeam
                existing.awayTeam = af.awayTeam
                existing.homeTeamLogo = af.homeTeamLogo
                existing.awayTeamLogo = af.awayTeamLogo
                existing.kickoff = Instant.ofEpochSecond(af.kickoff)
                existing.homeScore = af.homeScore
                existing.awayScore = af.awayScore
                existing.status = af.status
                existing.matchday = af.matchday
                existing.updatedAt = Instant.now()
                fixtureRepository.save(existing)

                if (wasNotFinished && af.status == "FINISHED") {
                    predictionService.calculatePoints(existing.id)
                }
            } else {
                fixtureRepository.save(
                    Fixture(
                        league = league,
                        apiFixtureId = af.fixtureId,
                        homeTeam = af.homeTeam,
                        awayTeam = af.awayTeam,
                        homeTeamLogo = af.homeTeamLogo,
                        awayTeamLogo = af.awayTeamLogo,
                        kickoff = Instant.ofEpochSecond(af.kickoff),
                        homeScore = af.homeScore,
                        awayScore = af.awayScore,
                        status = af.status,
                        matchday = af.matchday
                    )
                )
            }
        }
    }

    fun syncStandings(leagueIdStr: String, apiLeagueId: Int, season: Int) {
        val leagueId = java.util.UUID.fromString(leagueIdStr)
        val league = leagueRepository.findById(leagueId).orElse(null) ?: return

        val apiStandings = apiFootballService.getStandings(apiLeagueId, season)
        log.info("Syncing ${apiStandings.size} standings for league ${league.name}")

        for (as_ in apiStandings) {
            val existing = standingRepository.findByLeagueIdAndApiTeamId(leagueId, as_.teamId)
            if (existing != null) {
                existing.teamName = as_.teamName
                existing.teamLogo = as_.teamLogo
                existing.rank = as_.rank
                existing.points = as_.points
                existing.played = as_.played
                existing.won = as_.won
                existing.drawn = as_.drawn
                existing.lost = as_.lost
                existing.goalsFor = as_.goalsFor
                existing.goalsAgainst = as_.goalsAgainst
                existing.goalDiff = as_.goalDiff
                existing.updatedAt = Instant.now()
                standingRepository.save(existing)
            } else {
                standingRepository.save(
                    Standing(
                        league = league,
                        apiTeamId = as_.teamId,
                        teamName = as_.teamName,
                        teamLogo = as_.teamLogo,
                        rank = as_.rank,
                        points = as_.points,
                        played = as_.played,
                        won = as_.won,
                        drawn = as_.drawn,
                        lost = as_.lost,
                        goalsFor = as_.goalsFor,
                        goalsAgainst = as_.goalsAgainst,
                        goalDiff = as_.goalDiff
                    )
                )
            }
        }
    }

    fun syncSquad(leagueIdStr: String, apiLeagueId: Int, season: Int) {
        val leagueId = java.util.UUID.fromString(leagueIdStr)
        val league = leagueRepository.findById(leagueId).orElse(null) ?: return

        val teams = apiFootballService.getTeams(apiLeagueId, season)
        log.info("Syncing squads for ${teams.size} teams in league ${league.name}")

        for (team in teams) {
            val players = apiFootballService.getSquad(team.teamId)
            for (player in players) {
                val existing = playerRepository.findByLeagueIdAndApiPlayerId(leagueId, player.playerId)
                if (existing != null) {
                    existing.name = player.name
                    existing.photoUrl = player.photo
                    existing.position = player.position
                    playerRepository.save(existing)
                } else {
                    playerRepository.save(
                        Player(
                            apiPlayerId = player.playerId,
                            apiTeamId = team.teamId,
                            league = league,
                            name = player.name,
                            photoUrl = player.photo,
                            position = player.position
                        )
                    )
                }
            }
        }
    }
}
