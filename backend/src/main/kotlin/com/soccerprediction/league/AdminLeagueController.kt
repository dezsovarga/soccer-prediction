package com.soccerprediction.league

import com.soccerprediction.apifootball.ApiFootballService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/leagues")
class AdminLeagueController(
    private val leagueService: LeagueService,
    private val apiFootballService: ApiFootballService
) {
    @PostMapping
    fun createLeague(@RequestBody request: CreateLeagueRequest): ResponseEntity<LeagueDto> {
        val league = leagueService.createLeague(request)
        val memberCount = leagueService.getMemberCount(league.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(league.toDto(memberCount))
    }

    @PutMapping("/{id}")
    fun updateLeague(
        @PathVariable id: java.util.UUID,
        @RequestBody request: UpdateLeagueRequest
    ): ResponseEntity<LeagueDto> {
        val league = leagueService.updateLeague(id, request)
        val memberCount = leagueService.getMemberCount(league.id)
        return ResponseEntity.ok(league.toDto(memberCount))
    }

    @GetMapping
    fun getAllLeagues(): ResponseEntity<List<LeagueDto>> {
        val leagues = leagueService.getAllLeagues()
        val dtos = leagues.map { it.toDto(leagueService.getMemberCount(it.id)) }
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/search")
    fun searchApiFootballLeagues(@RequestParam query: String): ResponseEntity<Any> {
        val leagues = apiFootballService.searchLeagues(query)
        return ResponseEntity.ok(leagues)
    }
}
