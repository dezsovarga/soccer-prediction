package com.soccerprediction.apifootball

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ApiFootballService(
    @Qualifier("apiFootballRestTemplate") private val restTemplate: RestTemplate
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun searchLeagues(query: String): List<ApiFootballLeague> {
        log.info("Searching API-Football leagues with query: $query")
        try {
            val response = restTemplate.getForObject(
                "/leagues?search={query}",
                JsonNode::class.java,
                query
            )
            if (response == null) {
                log.warn("API-Football returned null response for league search")
                return emptyList()
            }
            log.info("API-Football response errors: ${response["errors"]}, results: ${response["results"]}")
            return parseLeagues(response)
        } catch (e: Exception) {
            log.error("API-Football league search failed: ${e.message}", e)
            throw e
        }
    }

    fun getLeaguesByCountry(country: String): List<ApiFootballLeague> {
        val response = restTemplate.getForObject(
            "/leagues?country={country}",
            JsonNode::class.java,
            country
        ) ?: return emptyList()

        return parseLeagues(response)
    }

    fun getFixtures(leagueId: Int, season: Int): List<ApiFootballFixture> {
        val response = restTemplate.getForObject(
            "/fixtures?league={leagueId}&season={season}",
            JsonNode::class.java,
            leagueId,
            season
        ) ?: return emptyList()

        return response["response"]?.mapNotNull { node ->
            try {
                ApiFootballFixture(
                    fixtureId = node["fixture"]["id"].asInt(),
                    homeTeam = node["teams"]["home"]["name"].asText(),
                    awayTeam = node["teams"]["away"]["name"].asText(),
                    homeTeamLogo = node["teams"]["home"]["logo"].asText(),
                    awayTeamLogo = node["teams"]["away"]["logo"].asText(),
                    kickoff = node["fixture"]["timestamp"].asLong(),
                    homeScore = node["goals"]["home"]?.takeIf { !it.isNull }?.asInt(),
                    awayScore = node["goals"]["away"]?.takeIf { !it.isNull }?.asInt(),
                    status = mapStatus(node["fixture"]["status"]["short"].asText()),
                    matchday = node["league"]["round"]?.asText()?.extractMatchday() ?: 0
                )
            } catch (e: Exception) {
                log.warn("Failed to parse fixture: ${e.message}")
                null
            }
        } ?: emptyList()
    }

    fun getStandings(leagueId: Int, season: Int): List<ApiFootballStanding> {
        val response = restTemplate.getForObject(
            "/standings?league={leagueId}&season={season}",
            JsonNode::class.java,
            leagueId,
            season
        ) ?: return emptyList()

        return response["response"]?.firstOrNull()
            ?.get("league")?.get("standings")?.firstOrNull()
            ?.mapNotNull { node ->
                try {
                    ApiFootballStanding(
                        teamId = node["team"]["id"].asInt(),
                        teamName = node["team"]["name"].asText(),
                        teamLogo = node["team"]["logo"].asText(),
                        rank = node["rank"].asInt(),
                        points = node["points"].asInt(),
                        played = node["all"]["played"].asInt(),
                        won = node["all"]["win"].asInt(),
                        drawn = node["all"]["draw"].asInt(),
                        lost = node["all"]["lose"].asInt(),
                        goalsFor = node["all"]["goals"]["for"].asInt(),
                        goalsAgainst = node["all"]["goals"]["against"].asInt(),
                        goalDiff = node["goalsDiff"].asInt()
                    )
                } catch (e: Exception) {
                    log.warn("Failed to parse standing: ${e.message}")
                    null
                }
            } ?: emptyList()
    }

    fun getSquad(teamId: Int): List<ApiFootballPlayer> {
        val response = restTemplate.getForObject(
            "/players/squads?team={teamId}",
            JsonNode::class.java,
            teamId
        ) ?: return emptyList()

        return response["response"]?.firstOrNull()
            ?.get("players")?.mapNotNull { node ->
                try {
                    ApiFootballPlayer(
                        playerId = node["id"].asInt(),
                        name = node["name"].asText(),
                        photo = node["photo"]?.asText(),
                        position = node["position"]?.asText()
                    )
                } catch (e: Exception) {
                    log.warn("Failed to parse player: ${e.message}")
                    null
                }
            } ?: emptyList()
    }

    fun getTeams(leagueId: Int, season: Int): List<ApiFootballTeam> {
        val response = restTemplate.getForObject(
            "/teams?league={leagueId}&season={season}",
            JsonNode::class.java,
            leagueId,
            season
        ) ?: return emptyList()

        return response["response"]?.mapNotNull { node ->
            try {
                ApiFootballTeam(
                    teamId = node["team"]["id"].asInt(),
                    name = node["team"]["name"].asText()
                )
            } catch (e: Exception) {
                log.warn("Failed to parse team: ${e.message}")
                null
            }
        } ?: emptyList()
    }

    private fun parseLeagues(response: JsonNode): List<ApiFootballLeague> {
        return response["response"]?.mapNotNull { node ->
            try {
                val seasons = node["seasons"]?.map { it["year"].asInt() } ?: emptyList()
                ApiFootballLeague(
                    leagueId = node["league"]["id"].asInt(),
                    name = node["league"]["name"].asText(),
                    country = node["country"]["name"].asText(),
                    logo = node["league"]["logo"]?.asText(),
                    seasons = seasons
                )
            } catch (e: Exception) {
                log.warn("Failed to parse league: ${e.message}")
                null
            }
        } ?: emptyList()
    }

    private fun mapStatus(shortStatus: String): String = when (shortStatus) {
        "NS", "TBD" -> "SCHEDULED"
        "1H", "HT", "2H", "ET", "P", "BT", "LIVE" -> "LIVE"
        "FT", "AET", "PEN" -> "FINISHED"
        "PST", "SUSP", "INT" -> "POSTPONED"
        "CANC", "ABD", "AWD", "WO" -> "CANCELLED"
        else -> "SCHEDULED"
    }

    private fun String.extractMatchday(): Int {
        return Regex("\\d+").find(this)?.value?.toIntOrNull() ?: 0
    }
}
