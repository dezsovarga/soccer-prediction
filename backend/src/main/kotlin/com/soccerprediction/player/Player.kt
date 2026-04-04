package com.soccerprediction.player

import com.soccerprediction.league.League
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "players")
class Player(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "api_player_id", nullable = false)
    val apiPlayerId: Int,

    @Column(name = "api_team_id", nullable = false)
    val apiTeamId: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    val league: League,

    @Column(nullable = false)
    var name: String,

    @Column(name = "photo_url")
    var photoUrl: String? = null,

    @Column
    var position: String? = null
)
