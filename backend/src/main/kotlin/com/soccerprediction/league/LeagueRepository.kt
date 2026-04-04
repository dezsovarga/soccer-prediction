package com.soccerprediction.league

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LeagueRepository : JpaRepository<League, UUID> {
    fun findByJoinCode(joinCode: String): League?
}
