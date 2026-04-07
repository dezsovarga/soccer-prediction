package com.soccerprediction.prediction

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TopScorerPickRepository : JpaRepository<TopScorerPick, UUID> {
    fun findByUserIdAndLeagueId(userId: UUID, leagueId: UUID): TopScorerPick?
    fun findByLeagueId(leagueId: UUID): List<TopScorerPick>
}

interface LeagueWinnerPickRepository : JpaRepository<LeagueWinnerPick, UUID> {
    fun findByUserIdAndLeagueId(userId: UUID, leagueId: UUID): LeagueWinnerPick?
    fun findByLeagueId(leagueId: UUID): List<LeagueWinnerPick>
}
