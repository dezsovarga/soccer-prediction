package com.soccerprediction.team

import com.soccerprediction.league.LeagueMode
import com.soccerprediction.league.LeagueRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val leagueRepository: LeagueRepository
) {
    fun getTeams(leagueId: UUID): List<Team> {
        return teamRepository.findByLeagueIdOrderByGroupNameAscNameAsc(leagueId)
    }

    @Transactional
    fun createTeam(leagueId: UUID, request: CreateTeamRequest): Team {
        val league = leagueRepository.findById(leagueId)
            .orElseThrow { IllegalArgumentException("League not found") }
        require(league.mode == LeagueMode.MANUAL) { "Teams can only be added to manual leagues" }

        val logoUrl = request.countryCode?.let { "https://flagcdn.com/w80/${it.lowercase()}.png" }

        return teamRepository.save(
            Team(
                league = league,
                name = request.name,
                countryCode = request.countryCode?.lowercase(),
                logoUrl = logoUrl,
                groupName = request.groupName
            )
        )
    }

    @Transactional
    fun updateTeam(teamId: UUID, request: UpdateTeamRequest): Team {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        request.name?.let { team.name = it }
        request.countryCode?.let {
            team.countryCode = it.lowercase()
            team.logoUrl = "https://flagcdn.com/w80/${it.lowercase()}.png"
        }
        request.groupName?.let { team.groupName = it }

        return teamRepository.save(team)
    }

    @Transactional
    fun deleteTeam(teamId: UUID) {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }
        teamRepository.delete(team)
    }
}
