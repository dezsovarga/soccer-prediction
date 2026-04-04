package com.soccerprediction.standing

fun Standing.toDto() = StandingDto(
    id = id,
    apiTeamId = apiTeamId,
    teamName = teamName,
    teamLogo = teamLogo,
    rank = rank,
    points = points,
    played = played,
    won = won,
    drawn = drawn,
    lost = lost,
    goalsFor = goalsFor,
    goalsAgainst = goalsAgainst,
    goalDiff = goalDiff
)
