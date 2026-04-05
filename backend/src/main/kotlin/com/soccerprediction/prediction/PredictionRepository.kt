package com.soccerprediction.prediction

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PredictionRepository : JpaRepository<Prediction, UUID> {
    fun findByUserIdAndFixtureId(userId: UUID, fixtureId: UUID): Prediction?
    fun findByUserIdAndFixtureLeagueId(userId: UUID, leagueId: UUID): List<Prediction>
    fun findByFixtureId(fixtureId: UUID): List<Prediction>
    fun findByFixtureLeagueIdAndPointsEarnedIsNull(leagueId: UUID): List<Prediction>
}
