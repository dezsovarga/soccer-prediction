package com.soccerprediction.fixture

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FixtureRepository : JpaRepository<Fixture, UUID> {
    fun findByLeagueIdOrderByKickoff(leagueId: UUID): List<Fixture>
    fun findByLeagueIdAndMatchdayOrderByKickoff(leagueId: UUID, matchday: Int): List<Fixture>
    fun findByApiFixtureId(apiFixtureId: Int): Fixture?
    fun findByLeagueIdAndStatus(leagueId: UUID, status: String): List<Fixture>
}
