import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchAllLeagues, createLeague, searchApiFootballLeagues, updateLeagueSettings } from '@/lib/api';
import type { CreateLeagueRequest, UpdateLeagueRequest } from '@/lib/types';

export function useAdminLeagues() {
  return useQuery({
    queryKey: ['admin', 'leagues'],
    queryFn: fetchAllLeagues,
  });
}

export function useSearchApiFootballLeagues(query: string) {
  return useQuery({
    queryKey: ['admin', 'search-leagues', query],
    queryFn: () => searchApiFootballLeagues(query),
    enabled: query.length >= 2,
  });
}

export function useCreateLeague() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateLeagueRequest) => createLeague(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'leagues'] });
    },
  });
}

export function useUpdateLeague() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, request }: { id: string; request: UpdateLeagueRequest }) =>
      updateLeagueSettings(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'leagues'] });
    },
  });
}
