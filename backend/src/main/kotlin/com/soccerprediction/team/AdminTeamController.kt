package com.soccerprediction.team

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/admin")
class AdminTeamController(
    private val teamService: TeamService
) {
    @GetMapping("/leagues/{leagueId}/teams")
    fun getTeams(@PathVariable leagueId: UUID): List<TeamDto> {
        return teamService.getTeams(leagueId).map { it.toDto() }
    }

    @PostMapping("/leagues/{leagueId}/teams")
    @ResponseStatus(HttpStatus.CREATED)
    fun createTeam(
        @PathVariable leagueId: UUID,
        @RequestBody request: CreateTeamRequest
    ): TeamDto {
        return teamService.createTeam(leagueId, request).toDto()
    }

    @PutMapping("/teams/{teamId}")
    fun updateTeam(
        @PathVariable teamId: UUID,
        @RequestBody request: UpdateTeamRequest
    ): TeamDto {
        return teamService.updateTeam(teamId, request).toDto()
    }

    @DeleteMapping("/teams/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTeam(@PathVariable teamId: UUID) {
        teamService.deleteTeam(teamId)
    }
}
