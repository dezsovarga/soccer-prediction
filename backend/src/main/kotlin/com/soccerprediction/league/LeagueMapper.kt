package com.soccerprediction.league

fun League.toDto(memberCount: Long) = LeagueDto(
    id = id,
    name = name,
    apiLeagueId = apiLeagueId,
    season = season,
    joinCode = joinCode,
    exactScorePoints = exactScorePoints,
    correctOutcomePoints = correctOutcomePoints,
    wrongPredictionPoints = wrongPredictionPoints,
    topScorerBonus = topScorerBonus,
    leagueWinnerBonus = leagueWinnerBonus,
    memberCount = memberCount,
    createdAt = createdAt
)

fun League.toSummaryDto(memberCount: Long) = LeagueSummaryDto(
    id = id,
    name = name,
    season = season,
    memberCount = memberCount
)
