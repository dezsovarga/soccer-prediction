package com.soccerprediction.standing

import com.soccerprediction.league.League
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "standings")
class Standing(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    val league: League,

    @Column(name = "api_team_id", nullable = false)
    val apiTeamId: Int,

    @Column(name = "team_name", nullable = false)
    var teamName: String,

    @Column(name = "team_logo")
    var teamLogo: String? = null,

    @Column(nullable = false)
    var rank: Int,

    @Column(nullable = false)
    var points: Int,

    @Column(nullable = false)
    var played: Int,

    @Column(nullable = false)
    var won: Int,

    @Column(nullable = false)
    var drawn: Int,

    @Column(nullable = false)
    var lost: Int,

    @Column(name = "goals_for", nullable = false)
    var goalsFor: Int,

    @Column(name = "goals_against", nullable = false)
    var goalsAgainst: Int,

    @Column(name = "goal_diff", nullable = false)
    var goalDiff: Int,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
