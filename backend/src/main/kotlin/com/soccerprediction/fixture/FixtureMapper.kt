package com.soccerprediction.fixture

fun Fixture.toDto() = FixtureDto(
    id = id,
    apiFixtureId = apiFixtureId,
    homeTeam = homeTeam,
    awayTeam = awayTeam,
    homeTeamLogo = homeTeamLogo,
    awayTeamLogo = awayTeamLogo,
    kickoff = kickoff,
    homeScore = homeScore,
    awayScore = awayScore,
    status = status,
    round = round,
    matchday = matchday
)
