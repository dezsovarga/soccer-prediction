package com.soccerprediction.prediction

import java.util.UUID

data class TopScorerPickDto(
    val id: UUID,
    val playerName: String,
    val apiPlayerId: Int?,
    val pointsEarned: Int?
)

data class TopScorerPickRequest(
    val playerName: String,
    val apiPlayerId: Int? = null
)

data class LeagueWinnerPickDto(
    val id: UUID,
    val teamName: String,
    val apiTeamId: Int?,
    val pointsEarned: Int?
)

data class LeagueWinnerPickRequest(
    val teamName: String,
    val apiTeamId: Int? = null
)

data class PlayerDto(
    val apiPlayerId: Int,
    val name: String,
    val photoUrl: String?,
    val position: String?,
    val apiTeamId: Int
)

fun TopScorerPick.toDto() = TopScorerPickDto(
    id = id,
    playerName = playerName,
    apiPlayerId = apiPlayerId,
    pointsEarned = pointsEarned
)

fun LeagueWinnerPick.toDto() = LeagueWinnerPickDto(
    id = id,
    teamName = teamName,
    apiTeamId = apiTeamId,
    pointsEarned = pointsEarned
)

fun com.soccerprediction.player.Player.toDto() = PlayerDto(
    apiPlayerId = apiPlayerId,
    name = name,
    photoUrl = photoUrl,
    position = position,
    apiTeamId = apiTeamId
)
