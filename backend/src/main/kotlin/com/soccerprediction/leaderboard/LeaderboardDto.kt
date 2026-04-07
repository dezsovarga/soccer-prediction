package com.soccerprediction.leaderboard

import java.util.UUID

data class LeaderboardEntryDto(
    val userId: UUID,
    val displayName: String,
    val pictureUrl: String?,
    val rank: Int,
    val totalPoints: Int,
    val correctScores: Int,
    val correctOutcomes: Int,
    val wrongPredictions: Int,
    val topScorerPoints: Int?,
    val leagueWinnerPoints: Int?
)
