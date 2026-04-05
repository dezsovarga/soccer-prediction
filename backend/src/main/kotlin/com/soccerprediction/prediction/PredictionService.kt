package com.soccerprediction.prediction

import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.league.LeagueMemberRepository
import com.soccerprediction.user.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PredictionService(
    private val predictionRepository: PredictionRepository,
    private val fixtureRepository: FixtureRepository,
    private val leagueMemberRepository: LeagueMemberRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createOrUpdatePrediction(user: User, fixtureId: UUID, request: PredictionRequest): Prediction {
        val fixture = fixtureRepository.findById(fixtureId)
            .orElseThrow { IllegalArgumentException("Fixture not found") }

        val membership = leagueMemberRepository.findByLeagueIdAndUserId(fixture.league.id, user.id)
            ?: throw IllegalAccessException("You are not a member of this league")

        if (fixture.kickoff.isBefore(Instant.now())) {
            throw IllegalStateException("Cannot predict after kickoff")
        }

        val existing = predictionRepository.findByUserIdAndFixtureId(user.id, fixtureId)
        if (existing != null) {
            existing.homeScore = request.homeScore
            existing.awayScore = request.awayScore
            existing.updatedAt = Instant.now()
            return predictionRepository.save(existing)
        }

        return predictionRepository.save(
            Prediction(
                user = user,
                fixture = fixture,
                homeScore = request.homeScore,
                awayScore = request.awayScore
            )
        )
    }

    fun getUserPredictions(userId: UUID, leagueId: UUID): List<Prediction> {
        return predictionRepository.findByUserIdAndFixtureLeagueId(userId, leagueId)
    }

    @Transactional
    fun calculatePoints(fixtureId: UUID) {
        val fixture = fixtureRepository.findById(fixtureId)
            .orElseThrow { IllegalArgumentException("Fixture not found") }

        if (fixture.status != "FINISHED" || fixture.homeScore == null || fixture.awayScore == null) {
            return
        }

        val league = fixture.league
        val predictions = predictionRepository.findByFixtureId(fixtureId)

        for (prediction in predictions) {
            prediction.pointsEarned = calculatePointsForPrediction(
                predictedHome = prediction.homeScore,
                predictedAway = prediction.awayScore,
                actualHome = fixture.homeScore!!,
                actualAway = fixture.awayScore!!,
                exactScorePoints = league.exactScorePoints,
                correctOutcomePoints = league.correctOutcomePoints,
                wrongPredictionPoints = league.wrongPredictionPoints
            )
            prediction.updatedAt = Instant.now()
        }

        predictionRepository.saveAll(predictions)
        log.info("Calculated points for ${predictions.size} predictions on fixture $fixtureId")
    }

    companion object {
        fun calculatePointsForPrediction(
            predictedHome: Int,
            predictedAway: Int,
            actualHome: Int,
            actualAway: Int,
            exactScorePoints: Int,
            correctOutcomePoints: Int,
            wrongPredictionPoints: Int
        ): Int {
            if (predictedHome == actualHome && predictedAway == actualAway) {
                return exactScorePoints
            }

            val predictedOutcome = predictedHome.compareTo(predictedAway)
            val actualOutcome = actualHome.compareTo(actualAway)

            if (predictedOutcome == actualOutcome) {
                return correctOutcomePoints
            }

            return wrongPredictionPoints
        }
    }
}
