package com.soccerprediction.prediction

import com.soccerprediction.league.League
import com.soccerprediction.user.User
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "top_scorer_picks",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "league_id"])]
)
class TopScorerPick(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    val league: League,

    @Column(name = "player_name", nullable = false)
    var playerName: String,

    @Column(name = "api_player_id")
    var apiPlayerId: Int? = null,

    @Column(name = "points_earned")
    var pointsEarned: Int? = null
)
