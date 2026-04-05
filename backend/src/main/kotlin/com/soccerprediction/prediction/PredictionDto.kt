package com.soccerprediction.prediction

import java.util.UUID

data class PredictionDto(
    val id: UUID,
    val fixtureId: UUID,
    val homeScore: Int,
    val awayScore: Int,
    val pointsEarned: Int?,
    val fixtureHomeTeam: String,
    val fixtureAwayTeam: String,
    val fixtureHomeScore: Int?,
    val fixtureAwayScore: Int?,
    val fixtureKickoff: String,
    val fixtureStatus: String,
    val matchday: Int
)

data class PredictionRequest(
    val homeScore: Int,
    val awayScore: Int
)
