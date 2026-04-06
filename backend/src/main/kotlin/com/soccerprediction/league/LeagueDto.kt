package com.soccerprediction.league

import java.time.Instant
import java.util.UUID

data class LeagueDto(
    val id: UUID,
    val name: String,
    val mode: String,
    val apiLeagueId: Int?,
    val season: Int,
    val joinCode: String,
    val exactScorePoints: Int,
    val correctOutcomePoints: Int,
    val wrongPredictionPoints: Int,
    val topScorerBonus: Int,
    val leagueWinnerBonus: Int,
    val memberCount: Long,
    val createdAt: Instant
)

data class LeagueSummaryDto(
    val id: UUID,
    val name: String,
    val season: Int,
    val memberCount: Long
)

data class CreateLeagueRequest(
    val name: String,
    val mode: String = "API_SYNCED",
    val apiLeagueId: Int? = null,
    val season: Int,
    val exactScorePoints: Int = 3,
    val correctOutcomePoints: Int = 1,
    val wrongPredictionPoints: Int = 0,
    val topScorerBonus: Int = 10,
    val leagueWinnerBonus: Int = 10
)

data class UpdateLeagueRequest(
    val name: String? = null,
    val exactScorePoints: Int? = null,
    val correctOutcomePoints: Int? = null,
    val wrongPredictionPoints: Int? = null,
    val topScorerBonus: Int? = null,
    val leagueWinnerBonus: Int? = null
)

data class JoinLeagueRequest(
    val joinCode: String
)
