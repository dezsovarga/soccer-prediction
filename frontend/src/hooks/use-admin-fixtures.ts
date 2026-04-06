import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchAdminFixtures, createFixture, updateFixture, deleteFixture, enterResult } from '@/lib/api';
import type { CreateFixtureRequest, UpdateFixtureRequest, EnterResultRequest } from '@/lib/types';

export function useAdminFixtures(leagueId: string) {
  return useQuery({
    queryKey: ['admin', 'fixtures', leagueId],
    queryFn: () => fetchAdminFixtures(leagueId),
  });
}

export function useCreateFixture(leagueId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateFixtureRequest) => createFixture(leagueId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'fixtures', leagueId] });
    },
  });
}

export function useUpdateFixture(leagueId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ fixtureId, request }: { fixtureId: string; request: UpdateFixtureRequest }) =>
      updateFixture(fixtureId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'fixtures', leagueId] });
    },
  });
}

export function useDeleteFixture(leagueId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (fixtureId: string) => deleteFixture(fixtureId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'fixtures', leagueId] });
    },
  });
}

export function useEnterResult(leagueId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ fixtureId, request }: { fixtureId: string; request: EnterResultRequest }) =>
      enterResult(fixtureId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'fixtures', leagueId] });
      queryClient.invalidateQueries({ queryKey: ['standings'] });
    },
  });
}
