import { useQueries } from '@tanstack/react-query';
import { fetchFixtures, fetchMyPredictions, fetchLeaderboard } from '@/lib/api';
import type { LeagueSummaryDto, FixtureDto, PredictionDto } from '@/lib/types';

export interface UpcomingFixture {
  fixture: FixtureDto;
  leagueId: string;
  leagueName: string;
  predicted: boolean;
}

export interface RecentResult {
  prediction: PredictionDto;
  leagueId: string;
  leagueName: string;
}

export interface LeagueWithStats {
  league: LeagueSummaryDto;
  rank: number | null;
  totalPoints: number;
  upcomingCount: number;
}

export function useDashboardData(leagues: LeagueSummaryDto[] | undefined, userId: string | undefined) {
  const leagueIds = leagues?.map((l) => l.id) ?? [];

  const fixtureQueries = useQueries({
    queries: leagueIds.map((id) => ({
      queryKey: ['leagues', id, 'fixtures'],
      queryFn: () => fetchFixtures(id),
      enabled: !!leagues?.length,
    })),
  });

  const predictionQueries = useQueries({
    queries: leagueIds.map((id) => ({
      queryKey: ['predictions', id],
      queryFn: () => fetchMyPredictions(id),
      enabled: !!leagues?.length,
    })),
  });

  const leaderboardQueries = useQueries({
    queries: leagueIds.map((id) => ({
      queryKey: ['leagues', id, 'leaderboard'],
      queryFn: () => fetchLeaderboard(id),
      enabled: !!leagues?.length,
    })),
  });

  const isLoading = fixtureQueries.some((q) => q.isLoading) ||
    predictionQueries.some((q) => q.isLoading) ||
    leaderboardQueries.some((q) => q.isLoading);

  const now = new Date();

  const upcoming: UpcomingFixture[] = [];
  const recent: RecentResult[] = [];
  const leagueStats: LeagueWithStats[] = [];

  if (leagues) {
    for (let i = 0; i < leagues.length; i++) {
      const league = leagues[i];
      const fixtures = fixtureQueries[i]?.data ?? [];
      const predictions = predictionQueries[i]?.data ?? [];
      const leaderboard = leaderboardQueries[i]?.data ?? [];

      const predictedFixtureIds = new Set(predictions.map((p) => p.fixtureId));

      const scheduledFixtures = fixtures
        .filter((f) => f.status === 'SCHEDULED' && new Date(f.kickoff) > now)
        .sort((a, b) => new Date(a.kickoff).getTime() - new Date(b.kickoff).getTime());

      for (const fixture of scheduledFixtures) {
        upcoming.push({
          fixture,
          leagueId: league.id,
          leagueName: league.name,
          predicted: predictedFixtureIds.has(fixture.id),
        });
      }

      const finishedPredictions = predictions
        .filter((p) => p.fixtureStatus === 'FINISHED')
        .sort((a, b) => new Date(b.fixtureKickoff).getTime() - new Date(a.fixtureKickoff).getTime());

      for (const prediction of finishedPredictions) {
        recent.push({
          prediction,
          leagueId: league.id,
          leagueName: league.name,
        });
      }

      const myEntry = leaderboard.find((e) => e.userId === userId);
      leagueStats.push({
        league,
        rank: myEntry?.rank ?? null,
        totalPoints: myEntry?.totalPoints ?? 0,
        upcomingCount: scheduledFixtures.filter((f) => !predictedFixtureIds.has(f.id)).length,
      });
    }
  }

  upcoming.sort((a, b) => new Date(a.fixture.kickoff).getTime() - new Date(b.fixture.kickoff).getTime());
  recent.sort((a, b) => new Date(b.prediction.fixtureKickoff).getTime() - new Date(a.prediction.fixtureKickoff).getTime());

  return {
    upcoming: upcoming.slice(0, 6),
    recent: recent.slice(0, 8),
    leagueStats,
    isLoading,
  };
}
