package com.soccerprediction.fixture

import com.soccerprediction.league.League
import com.soccerprediction.team.Team
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "fixtures")
class Fixture(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    val league: League,

    @Column(name = "api_fixture_id")
    val apiFixtureId: Int? = null,

    @Column(name = "home_team", nullable = false)
    var homeTeam: String,

    @Column(name = "away_team", nullable = false)
    var awayTeam: String,

    @Column(name = "home_team_logo")
    var homeTeamLogo: String? = null,

    @Column(name = "away_team_logo")
    var awayTeamLogo: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    var homeTeamRef: Team? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    var awayTeamRef: Team? = null,

    @Column(nullable = false)
    var kickoff: Instant,

    @Column(name = "home_score")
    var homeScore: Int? = null,

    @Column(name = "away_score")
    var awayScore: Int? = null,

    @Column(nullable = false)
    var status: String = "SCHEDULED",

    @Column
    var round: String? = null,

    @Column(nullable = false)
    var matchday: Int,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
