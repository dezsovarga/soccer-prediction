package com.soccerprediction.prediction

import com.soccerprediction.league.League
import com.soccerprediction.user.User
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "league_winner_picks",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "league_id"])]
)
class LeagueWinnerPick(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    val league: League,

    @Column(name = "team_name", nullable = false)
    var teamName: String,

    @Column(name = "api_team_id")
    var apiTeamId: Int? = null,

    @Column(name = "points_earned")
    var pointsEarned: Int? = null
)
