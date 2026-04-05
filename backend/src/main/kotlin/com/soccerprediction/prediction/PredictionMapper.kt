package com.soccerprediction.prediction

fun Prediction.toDto() = PredictionDto(
    id = id,
    fixtureId = fixture.id,
    homeScore = homeScore,
    awayScore = awayScore,
    pointsEarned = pointsEarned,
    fixtureHomeTeam = fixture.homeTeam,
    fixtureAwayTeam = fixture.awayTeam,
    fixtureHomeScore = fixture.homeScore,
    fixtureAwayScore = fixture.awayScore,
    fixtureKickoff = fixture.kickoff.toString(),
    fixtureStatus = fixture.status,
    matchday = fixture.matchday
)
