package com.soccerprediction.league

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LeagueMemberRepository : JpaRepository<LeagueMember, UUID> {
    fun findByUserId(userId: UUID): List<LeagueMember>
    fun findByLeagueIdAndUserId(leagueId: UUID, userId: UUID): LeagueMember?
    fun countByLeagueId(leagueId: UUID): Long
}
