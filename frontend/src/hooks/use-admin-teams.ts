import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchTeams, createTeam, updateTeam, deleteTeam } from '@/lib/api';
import type { CreateTeamRequest, UpdateTeamRequest } from '@/lib/types';

export function useTeams(leagueId: string) {
  return useQuery({
    queryKey: ['admin', 'teams', leagueId],
    queryFn: () => fetchTeams(leagueId),
  });
}

export function useCreateTeam(leagueId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateTeamRequest) => createTeam(leagueId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'teams', leagueId] });
    },
  });
}

export function useUpdateTeam(leagueId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ teamId, request }: { teamId: string; request: UpdateTeamRequest }) =>
      updateTeam(teamId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'teams', leagueId] });
    },
  });
}

export function useDeleteTeam(leagueId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (teamId: string) => deleteTeam(teamId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'teams', leagueId] });
    },
  });
}
