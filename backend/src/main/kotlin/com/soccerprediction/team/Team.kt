package com.soccerprediction.team

import com.soccerprediction.league.League
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "teams",
    uniqueConstraints = [UniqueConstraint(columnNames = ["league_id", "name"])]
)
class Team(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    val league: League,

    @Column(nullable = false)
    var name: String,

    @Column(name = "country_code")
    var countryCode: String? = null,

    @Column(name = "logo_url")
    var logoUrl: String? = null,

    @Column(name = "group_name")
    var groupName: String? = null,

    @Column(name = "api_team_id")
    var apiTeamId: Int? = null
)
