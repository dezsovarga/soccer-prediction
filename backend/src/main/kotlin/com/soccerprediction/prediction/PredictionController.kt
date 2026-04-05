package com.soccerprediction.prediction

import com.soccerprediction.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
class PredictionController(
    private val predictionService: PredictionService,
    private val pickService: PickService,
    private val userRepository: UserRepository
) {
    @PutMapping("/api/predictions/{fixtureId}")
    fun createOrUpdatePrediction(
        @AuthenticationPrincipal oauth2User: OAuth2User,
        @PathVariable fixtureId: UUID,
        @RequestBody request: PredictionRequest
    ): ResponseEntity<PredictionDto> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val prediction = predictionService.createOrUpdatePrediction(user, fixtureId, request)
            ResponseEntity.ok(prediction.toDto())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/api/leagues/{leagueId}/predictions/me")
    fun getMyPredictions(
        @AuthenticationPrincipal oauth2User: OAuth2User,
        @PathVariable leagueId: UUID
    ): ResponseEntity<List<PredictionDto>> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val predictions = predictionService.getUserPredictions(user.id, leagueId)
        return ResponseEntity.ok(predictions.map { it.toDto() })
    }

    @GetMapping("/api/leagues/{leagueId}/players")
    fun getPlayers(
        @AuthenticationPrincipal oauth2User: OAuth2User,
        @PathVariable leagueId: UUID
    ): ResponseEntity<List<PlayerDto>> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val players = pickService.getPlayersForLeague(leagueId)
        return ResponseEntity.ok(players.map { it.toDto() })
    }

    @PutMapping("/api/leagues/{leagueId}/top-scorer-pick")
    fun setTopScorerPick(
        @AuthenticationPrincipal oauth2User: OAuth2User,
        @PathVariable leagueId: UUID,
        @RequestBody request: TopScorerPickRequest
    ): ResponseEntity<TopScorerPickDto> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val pick = pickService.setTopScorerPick(user, leagueId, request)
            ResponseEntity.ok(pick.toDto())
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/api/leagues/{leagueId}/top-scorer-pick")
    fun getTopScorerPick(
        @AuthenticationPrincipal oauth2User: OAuth2User,
        @PathVariable leagueId: UUID
    ): ResponseEntity<TopScorerPickDto> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val pick = pickService.getTopScorerPick(user.id, leagueId)
            ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(pick.toDto())
    }

    @PutMapping("/api/leagues/{leagueId}/league-winner-pick")
    fun setLeagueWinnerPick(
        @AuthenticationPrincipal oauth2User: OAuth2User,
        @PathVariable leagueId: UUID,
        @RequestBody request: LeagueWinnerPickRequest
    ): ResponseEntity<LeagueWinnerPickDto> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val pick = pickService.setLeagueWinnerPick(user, leagueId, request)
            ResponseEntity.ok(pick.toDto())
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/api/leagues/{leagueId}/league-winner-pick")
    fun getLeagueWinnerPick(
        @AuthenticationPrincipal oauth2User: OAuth2User,
        @PathVariable leagueId: UUID
    ): ResponseEntity<LeagueWinnerPickDto> {
        val user = getUser(oauth2User) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val pick = pickService.getLeagueWinnerPick(user.id, leagueId)
            ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(pick.toDto())
    }

    private fun getUser(oauth2User: OAuth2User) =
        oauth2User.getAttribute<String>("email")?.let { userRepository.findByEmail(it) }
}
