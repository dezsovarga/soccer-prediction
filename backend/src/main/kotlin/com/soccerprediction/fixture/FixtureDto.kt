package com.soccerprediction.fixture

import java.time.Instant
import java.util.UUID

data class FixtureDto(
    val id: UUID,
    val apiFixtureId: Int?,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamLogo: String?,
    val awayTeamLogo: String?,
    val kickoff: Instant,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: String,
    val round: String?,
    val matchday: Int
)

data class CreateFixtureRequest(
    val homeTeamId: UUID,
    val awayTeamId: UUID,
    val kickoff: Instant,
    val round: String? = null,
    val matchday: Int
)

data class UpdateFixtureRequest(
    val homeTeamId: UUID? = null,
    val awayTeamId: UUID? = null,
    val kickoff: Instant? = null,
    val round: String? = null,
    val matchday: Int? = null
)

data class EnterResultRequest(
    val homeScore: Int,
    val awayScore: Int
)
