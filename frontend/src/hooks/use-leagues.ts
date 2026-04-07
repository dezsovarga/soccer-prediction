import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchMyLeagues, fetchLeague, fetchFixtures, fetchStandings, joinLeague, fetchLeaderboard } from '@/lib/api';

export function useMyLeagues() {
  return useQuery({
    queryKey: ['leagues', 'mine'],
    queryFn: fetchMyLeagues,
  });
}

export function useLeague(id: string) {
  return useQuery({
    queryKey: ['leagues', id],
    queryFn: () => fetchLeague(id),
    enabled: !!id,
  });
}

export function useFixtures(leagueId: string) {
  return useQuery({
    queryKey: ['leagues', leagueId, 'fixtures'],
    queryFn: () => fetchFixtures(leagueId),
    enabled: !!leagueId,
  });
}

export function useStandings(leagueId: string) {
  return useQuery({
    queryKey: ['leagues', leagueId, 'standings'],
    queryFn: () => fetchStandings(leagueId),
    enabled: !!leagueId,
  });
}

export function useLeaderboard(leagueId: string) {
  return useQuery({
    queryKey: ['leagues', leagueId, 'leaderboard'],
    queryFn: () => fetchLeaderboard(leagueId),
    enabled: !!leagueId,
  });
}

export function useJoinLeague() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (joinCode: string) => joinLeague(joinCode),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['leagues', 'mine'] });
    },
  });
}
