package com.soccerprediction.standing

import java.util.UUID

data class StandingDto(
    val id: UUID,
    val apiTeamId: Int,
    val teamName: String,
    val teamLogo: String?,
    val rank: Int,
    val points: Int,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDiff: Int
)
