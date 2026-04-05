import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchMyPredictions,
  savePrediction,
  fetchPlayers,
  fetchTopScorerPick,
  saveTopScorerPick,
  fetchLeagueWinnerPick,
  saveLeagueWinnerPick,
} from '@/lib/api';
import type { PredictionRequest, TopScorerPickRequest, LeagueWinnerPickRequest } from '@/lib/types';

export function useMyPredictions(leagueId: string) {
  return useQuery({
    queryKey: ['predictions', leagueId],
    queryFn: () => fetchMyPredictions(leagueId),
  });
}

export function useSavePrediction(leagueId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ fixtureId, request }: { fixtureId: string; request: PredictionRequest }) =>
      savePrediction(fixtureId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['predictions', leagueId] });
    },
  });
}

export function usePlayers(leagueId: string) {
  return useQuery({
    queryKey: ['players', leagueId],
    queryFn: () => fetchPlayers(leagueId),
  });
}

export function useTopScorerPick(leagueId: string) {
  return useQuery({
    queryKey: ['topScorerPick', leagueId],
    queryFn: () => fetchTopScorerPick(leagueId),
    retry: false,
  });
}

export function useSaveTopScorerPick(leagueId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: TopScorerPickRequest) => saveTopScorerPick(leagueId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['topScorerPick', leagueId] });
    },
  });
}

export function useLeagueWinnerPick(leagueId: string) {
  return useQuery({
    queryKey: ['leagueWinnerPick', leagueId],
    queryFn: () => fetchLeagueWinnerPick(leagueId),
    retry: false,
  });
}

export function useSaveLeagueWinnerPick(leagueId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: LeagueWinnerPickRequest) => saveLeagueWinnerPick(leagueId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['leagueWinnerPick', leagueId] });
    },
  });
}
