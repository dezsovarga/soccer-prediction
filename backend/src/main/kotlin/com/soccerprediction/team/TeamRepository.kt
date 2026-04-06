package com.soccerprediction.team

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TeamRepository : JpaRepository<Team, UUID> {
    fun findByLeagueId(leagueId: UUID): List<Team>
    fun findByLeagueIdOrderByGroupNameAscNameAsc(leagueId: UUID): List<Team>
    fun findByLeagueIdAndName(leagueId: UUID, name: String): Team?
}
