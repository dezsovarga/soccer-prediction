package com.soccerprediction.league

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "leagues")
class League(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(name = "api_league_id", nullable = false)
    val apiLeagueId: Int,

    @Column(nullable = false)
    val season: Int,

    @Column(name = "join_code", unique = true, nullable = false)
    val joinCode: String,

    @Column(name = "exact_score_points", nullable = false)
    var exactScorePoints: Int = 3,

    @Column(name = "correct_outcome_points", nullable = false)
    var correctOutcomePoints: Int = 1,

    @Column(name = "wrong_prediction_points", nullable = false)
    var wrongPredictionPoints: Int = 0,

    @Column(name = "top_scorer_bonus", nullable = false)
    var topScorerBonus: Int = 10,

    @Column(name = "league_winner_bonus", nullable = false)
    var leagueWinnerBonus: Int = 10,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
