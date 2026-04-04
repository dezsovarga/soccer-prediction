package com.soccerprediction.player

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PlayerRepository : JpaRepository<Player, UUID> {
    fun findByLeagueId(leagueId: UUID): List<Player>
    fun findByLeagueIdAndApiPlayerId(leagueId: UUID, apiPlayerId: Int): Player?
}
