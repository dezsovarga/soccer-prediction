package com.soccerprediction.league

import com.soccerprediction.apifootball.SyncService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LeagueService(
    private val leagueRepository: LeagueRepository,
    private val leagueMemberRepository: LeagueMemberRepository,
    private val syncService: SyncService
) {
    fun createLeague(request: CreateLeagueRequest): League {
        val mode = LeagueMode.valueOf(request.mode)

        if (mode == LeagueMode.API_SYNCED) {
            requireNotNull(request.apiLeagueId) { "apiLeagueId is required for API_SYNCED leagues" }
        }

        val joinCode = generateJoinCode()
        val league = leagueRepository.save(
            League(
                name = request.name,
                mode = mode,
                apiLeagueId = request.apiLeagueId,
                season = request.season,
                joinCode = joinCode,
                exactScorePoints = request.exactScorePoints,
                correctOutcomePoints = request.correctOutcomePoints,
                wrongPredictionPoints = request.wrongPredictionPoints,
                topScorerBonus = request.topScorerBonus,
                leagueWinnerBonus = request.leagueWinnerBonus
            )
        )

        if (mode == LeagueMode.API_SYNCED) {
            try {
                syncService.syncFixtures(league.id.toString(), league.apiLeagueId!!, league.season)
                syncService.syncStandings(league.id.toString(), league.apiLeagueId!!, league.season)
                syncService.syncSquad(league.id.toString(), league.apiLeagueId!!, league.season)
            } catch (e: Exception) {
                // League is created; sync failures are non-fatal
            }
        }

        return league
    }

    fun updateLeague(id: UUID, request: UpdateLeagueRequest): League {
        val league = leagueRepository.findById(id)
            .orElseThrow { IllegalArgumentException("League not found") }

        request.name?.let { league.name = it }
        request.exactScorePoints?.let { league.exactScorePoints = it }
        request.correctOutcomePoints?.let { league.correctOutcomePoints = it }
        request.wrongPredictionPoints?.let { league.wrongPredictionPoints = it }
        request.topScorerBonus?.let { league.topScorerBonus = it }
        request.leagueWinnerBonus?.let { league.leagueWinnerBonus = it }

        return leagueRepository.save(league)
    }

    fun getLeagueById(id: UUID): League {
        return leagueRepository.findById(id)
            .orElseThrow { IllegalArgumentException("League not found") }
    }

    fun getMemberCount(leagueId: UUID): Long {
        return leagueMemberRepository.countByLeagueId(leagueId)
    }

    fun getAllLeagues(): List<League> {
        return leagueRepository.findAll()
    }

    private fun generateJoinCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }
}
