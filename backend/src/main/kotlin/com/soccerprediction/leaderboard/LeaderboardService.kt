package com.soccerprediction.leaderboard

import com.soccerprediction.league.LeagueMemberRepository
import com.soccerprediction.league.LeagueRepository
import com.soccerprediction.prediction.LeagueWinnerPickRepository
import com.soccerprediction.prediction.PredictionRepository
import com.soccerprediction.prediction.TopScorerPickRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LeaderboardService(
    private val leagueRepository: LeagueRepository,
    private val leagueMemberRepository: LeagueMemberRepository,
    private val predictionRepository: PredictionRepository,
    private val topScorerPickRepository: TopScorerPickRepository,
    private val leagueWinnerPickRepository: LeagueWinnerPickRepository
) {

    fun getLeaderboard(leagueId: UUID): List<LeaderboardEntryDto> {
        val league = leagueRepository.findById(leagueId)
            .orElseThrow { IllegalArgumentException("League not found") }

        val members = leagueMemberRepository.findByLeagueId(leagueId)
        val predictions = predictionRepository.findByFixtureLeagueId(leagueId)
        val topScorerPicks = topScorerPickRepository.findByLeagueId(leagueId)
        val leagueWinnerPicks = leagueWinnerPickRepository.findByLeagueId(leagueId)

        val predictionsByUser = predictions.groupBy { it.user.id }
        val topScorerByUser = topScorerPicks.associateBy { it.user.id }
        val leagueWinnerByUser = leagueWinnerPicks.associateBy { it.user.id }

        val entries = members.map { member ->
            val user = member.user
            val userPredictions = predictionsByUser[user.id] ?: emptyList()
            val scoredPredictions = userPredictions.filter { it.pointsEarned != null }

            val correctScores = scoredPredictions.count { it.pointsEarned == league.exactScorePoints }
            val correctOutcomes = scoredPredictions.count {
                it.pointsEarned == league.correctOutcomePoints && it.pointsEarned != league.exactScorePoints
            }
            val wrongPredictions = scoredPredictions.count { it.pointsEarned == league.wrongPredictionPoints && league.wrongPredictionPoints != league.correctOutcomePoints }

            val predictionPoints = scoredPredictions.sumOf { it.pointsEarned ?: 0 }
            val topScorerPick = topScorerByUser[user.id]
            val leagueWinnerPick = leagueWinnerByUser[user.id]
            val topScorerPoints = topScorerPick?.pointsEarned
            val leagueWinnerPoints = leagueWinnerPick?.pointsEarned

            val totalPoints = predictionPoints + (topScorerPoints ?: 0) + (leagueWinnerPoints ?: 0)

            LeaderboardEntryDto(
                userId = user.id,
                displayName = user.displayName,
                pictureUrl = user.pictureUrl,
                rank = 0,
                totalPoints = totalPoints,
                correctScores = correctScores,
                correctOutcomes = correctOutcomes,
                wrongPredictions = wrongPredictions,
                topScorerPoints = topScorerPoints,
                leagueWinnerPoints = leagueWinnerPoints
            )
        }

        return entries
            .sortedWith(
                compareByDescending<LeaderboardEntryDto> { it.totalPoints }
                    .thenByDescending { it.correctScores }
                    .thenByDescending { it.correctOutcomes }
                    .thenBy { it.displayName }
            )
            .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
    }
}
