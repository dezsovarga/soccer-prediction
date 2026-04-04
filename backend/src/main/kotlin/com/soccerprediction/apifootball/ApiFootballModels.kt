package com.soccerprediction.apifootball

data class ApiFootballLeague(
    val leagueId: Int,
    val name: String,
    val country: String,
    val logo: String?,
    val seasons: List<Int>
)

data class ApiFootballFixture(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamLogo: String?,
    val awayTeamLogo: String?,
    val kickoff: Long,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: String,
    val matchday: Int
)

data class ApiFootballStanding(
    val teamId: Int,
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

data class ApiFootballPlayer(
    val playerId: Int,
    val name: String,
    val photo: String?,
    val position: String?
)

data class ApiFootballTeam(
    val teamId: Int,
    val name: String
)
