package com.soccerprediction.leaderboard

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class LeaderboardController(
    private val leaderboardService: LeaderboardService
) {

    @GetMapping("/api/leagues/{leagueId}/leaderboard")
    fun getLeaderboard(
        @AuthenticationPrincipal oauth2User: OAuth2User?,
        @PathVariable leagueId: UUID
    ): ResponseEntity<List<LeaderboardEntryDto>> {
        if (oauth2User == null) return ResponseEntity.status(401).build()

        return try {
            val leaderboard = leaderboardService.getLeaderboard(leagueId)
            ResponseEntity.ok(leaderboard)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
