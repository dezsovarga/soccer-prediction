package com.soccerprediction.prediction

import com.soccerprediction.fixture.FixtureRepository
import com.soccerprediction.league.LeagueMemberRepository
import com.soccerprediction.player.PlayerRepository
import com.soccerprediction.standing.StandingRepository
import com.soccerprediction.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PickService(
    private val topScorerPickRepository: TopScorerPickRepository,
    private val leagueWinnerPickRepository: LeagueWinnerPickRepository,
    private val fixtureRepository: FixtureRepository,
    private val leagueMemberRepository: LeagueMemberRepository,
    private val playerRepository: PlayerRepository,
    private val standingRepository: StandingRepository
) {
    @Transactional
    fun setTopScorerPick(user: User, leagueId: UUID, request: TopScorerPickRequest): TopScorerPick {
        validateMembership(user, leagueId)
        validatePicksNotLocked(leagueId)

        val existing = topScorerPickRepository.findByUserIdAndLeagueId(user.id, leagueId)
        if (existing != null) {
            existing.playerName = request.playerName
            existing.apiPlayerId = request.apiPlayerId
            return topScorerPickRepository.save(existing)
        }

        return topScorerPickRepository.save(
            TopScorerPick(
                user = user,
                league = leagueMemberRepository.findByLeagueIdAndUserId(leagueId, user.id)!!.league,
                playerName = request.playerName,
                apiPlayerId = request.apiPlayerId
            )
        )
    }

    @Transactional
    fun setLeagueWinnerPick(user: User, leagueId: UUID, request: LeagueWinnerPickRequest): LeagueWinnerPick {
        validateMembership(user, leagueId)
        validatePicksNotLocked(leagueId)

        val existing = leagueWinnerPickRepository.findByUserIdAndLeagueId(user.id, leagueId)
        if (existing != null) {
            existing.teamName = request.teamName
            existing.apiTeamId = request.apiTeamId
            return leagueWinnerPickRepository.save(existing)
        }

        return leagueWinnerPickRepository.save(
            LeagueWinnerPick(
                user = user,
                league = leagueMemberRepository.findByLeagueIdAndUserId(leagueId, user.id)!!.league,
                teamName = request.teamName,
                apiTeamId = request.apiTeamId
            )
        )
    }

    fun getTopScorerPick(userId: UUID, leagueId: UUID): TopScorerPick? {
        return topScorerPickRepository.findByUserIdAndLeagueId(userId, leagueId)
    }

    fun getLeagueWinnerPick(userId: UUID, leagueId: UUID): LeagueWinnerPick? {
        return leagueWinnerPickRepository.findByUserIdAndLeagueId(userId, leagueId)
    }

    fun getPlayersForLeague(leagueId: UUID): List<com.soccerprediction.player.Player> {
        return playerRepository.findByLeagueId(leagueId)
    }

    fun getTeamsForLeague(leagueId: UUID): List<com.soccerprediction.standing.Standing> {
        return standingRepository.findByLeagueIdOrderByRank(leagueId)
    }

    private fun validateMembership(user: User, leagueId: UUID) {
        leagueMemberRepository.findByLeagueIdAndUserId(leagueId, user.id)
            ?: throw IllegalAccessException("You are not a member of this league")
    }

    private fun validatePicksNotLocked(leagueId: UUID) {
        val fixtures = fixtureRepository.findByLeagueIdOrderByKickoff(leagueId)
        val firstKickoff = fixtures.firstOrNull()?.kickoff
        if (firstKickoff != null && firstKickoff.isBefore(Instant.now())) {
            throw IllegalStateException("Picks are locked after the first match kicks off")
        }
    }
}
