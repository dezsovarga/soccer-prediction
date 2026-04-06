package com.soccerprediction.standing

import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.team.TeamRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class StandingComputeService(
    private val standingRepository: StandingRepository,
    private val fixtureRepository: FixtureRepository,
    private val teamRepository: TeamRepository,
    private val leagueRepository: LeagueRepository
) {
    @Transactional
    fun recomputeStandings(leagueId: UUID) {
        val league = leagueRepository.findById(leagueId).orElse(null) ?: return
        val teams = teamRepository.findByLeagueId(leagueId)
        val finishedFixtures = fixtureRepository.findByLeagueIdAndStatus(leagueId, "FINISHED")

        data class Stats(
            var played: Int = 0,
            var won: Int = 0,
            var drawn: Int = 0,
            var lost: Int = 0,
            var goalsFor: Int = 0,
            var goalsAgainst: Int = 0
        ) {
            val goalDiff get() = goalsFor - goalsAgainst
            val points get() = won * 3 + drawn
        }

        val statsMap = teams.associate { it.id to Stats() }.toMutableMap()

        for (fixture in finishedFixtures) {
            val homeId = fixture.homeTeamRef?.id ?: continue
            val awayId = fixture.awayTeamRef?.id ?: continue
            val hs = fixture.homeScore ?: continue
            val as_ = fixture.awayScore ?: continue

            val homeStats = statsMap[homeId] ?: continue
            val awayStats = statsMap[awayId] ?: continue

            homeStats.played++
            awayStats.played++
            homeStats.goalsFor += hs
            homeStats.goalsAgainst += as_
            awayStats.goalsFor += as_
            awayStats.goalsAgainst += hs

            when {
                hs > as_ -> { homeStats.won++; awayStats.lost++ }
                hs < as_ -> { awayStats.won++; homeStats.lost++ }
                else -> { homeStats.drawn++; awayStats.drawn++ }
            }
        }

        // Delete existing standings for this league and rebuild
        standingRepository.deleteByLeagueId(leagueId)

        // Group teams by group name, rank within each group
        val teamsByGroup = teams.groupBy { it.groupName ?: "" }

        for ((_, groupTeams) in teamsByGroup) {
            val ranked = groupTeams.sortedWith(
                compareByDescending<com.soccerprediction.team.Team> { statsMap[it.id]?.points ?: 0 }
                    .thenByDescending { statsMap[it.id]?.goalDiff ?: 0 }
                    .thenByDescending { statsMap[it.id]?.goalsFor ?: 0 }
                    .thenBy { it.name }
            )

            ranked.forEachIndexed { index, team ->
                val stats = statsMap[team.id] ?: Stats()
                standingRepository.save(
                    Standing(
                        league = league,
                        team = team,
                        teamName = team.name,
                        teamLogo = team.logoUrl ?: team.countryCode?.let {
                            "https://flagcdn.com/w80/${it.lowercase()}.png"
                        },
                        groupName = team.groupName,
                        rank = index + 1,
                        points = stats.points,
                        played = stats.played,
                        won = stats.won,
                        drawn = stats.drawn,
                        lost = stats.lost,
                        goalsFor = stats.goalsFor,
                        goalsAgainst = stats.goalsAgainst,
                        goalDiff = stats.goalDiff,
                        updatedAt = Instant.now()
                    )
                )
            }
        }
    }
}
