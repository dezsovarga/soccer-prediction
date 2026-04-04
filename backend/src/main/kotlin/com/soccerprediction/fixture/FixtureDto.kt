package com.soccerprediction.fixture

import java.time.Instant
import java.util.UUID

data class FixtureDto(
    val id: UUID,
    val apiFixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamLogo: String?,
    val awayTeamLogo: String?,
    val kickoff: Instant,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: String,
    val matchday: Int
)
