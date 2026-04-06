package com.soccerprediction.standing

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StandingRepository : JpaRepository<Standing, UUID> {
    fun findByLeagueIdOrderByRank(leagueId: UUID): List<Standing>
    fun findByLeagueIdAndApiTeamId(leagueId: UUID, apiTeamId: Int): Standing?
    fun findByLeagueIdAndTeamId(leagueId: UUID, teamId: UUID): Standing?
    fun deleteByLeagueId(leagueId: UUID)
}
