import type { ErrorResponse, LeagueSummaryDto, LeagueDto, FixtureDto, StandingDto, ApiFootballLeague, CreateLeagueRequest, JoinLeagueRequest, PredictionDto, PredictionRequest, TopScorerPickDto, TopScorerPickRequest, LeagueWinnerPickDto, LeagueWinnerPickRequest, PlayerDto, TeamDto, CreateTeamRequest, UpdateTeamRequest, CreateFixtureRequest, UpdateFixtureRequest, EnterResultRequest, LeaderboardEntryDto, AdminUserDto, UpdateUserRequest, UpdateLeagueRequest } from './types';

const API_BASE = import.meta.env.VITE_API_URL || '';
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';

export class ApiError extends Error {
  status: number;
  body: ErrorResponse;

  constructor(status: number, body: ErrorResponse) {
    super(body.error);
    this.status = status;
    this.body = body;
  }
}

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  if (!res.ok) {
    const body = await res.json().catch(() => ({
      error: 'Unknown error',
      code: 'UNKNOWN',
    }));
    throw new ApiError(res.status, body);
  }

  return res.json();
}

export function getLoginUrl(): string {
  return `${BACKEND_URL}/api/auth/oauth2/authorize/google`;
}

export function getLogoutUrl(): string {
  return `${BACKEND_URL}/api/auth/logout`;
}

export function fetchMyLeagues(): Promise<LeagueSummaryDto[]> {
  return apiFetch<LeagueSummaryDto[]>('/api/leagues');
}

export function fetchLeague(id: string): Promise<LeagueDto> {
  return apiFetch<LeagueDto>(`/api/leagues/${id}`);
}

export function fetchFixtures(leagueId: string): Promise<FixtureDto[]> {
  return apiFetch<FixtureDto[]>(`/api/leagues/${leagueId}/fixtures`);
}

export function fetchStandings(leagueId: string): Promise<StandingDto[]> {
  return apiFetch<StandingDto[]>(`/api/leagues/${leagueId}/standings`);
}

export function joinLeague(joinCode: string): Promise<LeagueSummaryDto> {
  return apiFetch<LeagueSummaryDto>('/api/leagues/join', {
    method: 'POST',
    body: JSON.stringify({ joinCode } satisfies JoinLeagueRequest),
  });
}

export function searchApiFootballLeagues(query: string): Promise<ApiFootballLeague[]> {
  return apiFetch<ApiFootballLeague[]>(`/api/admin/leagues/search?query=${encodeURIComponent(query)}`);
}

export function createLeague(request: CreateLeagueRequest): Promise<LeagueDto> {
  return apiFetch<LeagueDto>('/api/admin/leagues', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export function fetchAllLeagues(): Promise<LeagueDto[]> {
  return apiFetch<LeagueDto[]>('/api/admin/leagues');
}

export function updateLeague(id: string, request: Partial<CreateLeagueRequest>): Promise<LeagueDto> {
  return apiFetch<LeagueDto>(`/api/admin/leagues/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}

export function savePrediction(fixtureId: string, request: PredictionRequest): Promise<PredictionDto> {
  return apiFetch<PredictionDto>(`/api/predictions/${fixtureId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}

export function fetchMyPredictions(leagueId: string): Promise<PredictionDto[]> {
  return apiFetch<PredictionDto[]>(`/api/leagues/${leagueId}/predictions/me`);
}

export function fetchPlayers(leagueId: string): Promise<PlayerDto[]> {
  return apiFetch<PlayerDto[]>(`/api/leagues/${leagueId}/players`);
}

export function fetchTopScorerPick(leagueId: string): Promise<TopScorerPickDto> {
  return apiFetch<TopScorerPickDto>(`/api/leagues/${leagueId}/top-scorer-pick`);
}

export function saveTopScorerPick(leagueId: string, request: TopScorerPickRequest): Promise<TopScorerPickDto> {
  return apiFetch<TopScorerPickDto>(`/api/leagues/${leagueId}/top-scorer-pick`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}

export function fetchLeagueWinnerPick(leagueId: string): Promise<LeagueWinnerPickDto> {
  return apiFetch<LeagueWinnerPickDto>(`/api/leagues/${leagueId}/league-winner-pick`);
}

export function saveLeagueWinnerPick(leagueId: string, request: LeagueWinnerPickRequest): Promise<LeagueWinnerPickDto> {
  return apiFetch<LeagueWinnerPickDto>(`/api/leagues/${leagueId}/league-winner-pick`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}

// Admin Team endpoints
export function fetchTeams(leagueId: string): Promise<TeamDto[]> {
  return apiFetch<TeamDto[]>(`/api/admin/leagues/${leagueId}/teams`);
}

export function createTeam(leagueId: string, request: CreateTeamRequest): Promise<TeamDto> {
  return apiFetch<TeamDto>(`/api/admin/leagues/${leagueId}/teams`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export function updateTeam(teamId: string, request: UpdateTeamRequest): Promise<TeamDto> {
  return apiFetch<TeamDto>(`/api/admin/teams/${teamId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}

export async function deleteTeam(teamId: string): Promise<void> {
  await apiFetch<void>(`/api/admin/teams/${teamId}`, { method: 'DELETE' });
}

// Admin Fixture endpoints
export function fetchAdminFixtures(leagueId: string): Promise<FixtureDto[]> {
  return apiFetch<FixtureDto[]>(`/api/admin/leagues/${leagueId}/fixtures`);
}

export function createFixture(leagueId: string, request: CreateFixtureRequest): Promise<FixtureDto> {
  return apiFetch<FixtureDto>(`/api/admin/leagues/${leagueId}/fixtures`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export function updateFixture(fixtureId: string, request: UpdateFixtureRequest): Promise<FixtureDto> {
  return apiFetch<FixtureDto>(`/api/admin/fixtures/${fixtureId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}

export async function deleteFixture(fixtureId: string): Promise<void> {
  await apiFetch<void>(`/api/admin/fixtures/${fixtureId}`, { method: 'DELETE' });
}

export function enterResult(fixtureId: string, request: EnterResultRequest): Promise<FixtureDto> {
  return apiFetch<FixtureDto>(`/api/admin/fixtures/${fixtureId}/result`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}

// Leaderboard
export function fetchLeaderboard(leagueId: string): Promise<LeaderboardEntryDto[]> {
  return apiFetch<LeaderboardEntryDto[]>(`/api/leagues/${leagueId}/leaderboard`);
}

// Admin User endpoints
export function fetchAdminUsers(): Promise<AdminUserDto[]> {
  return apiFetch<AdminUserDto[]>('/api/admin/users');
}

export function updateUser(id: string, request: UpdateUserRequest): Promise<AdminUserDto> {
  return apiFetch<AdminUserDto>(`/api/admin/users/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(request),
  });
}

// Admin League update (typed)
export function updateLeagueSettings(id: string, request: UpdateLeagueRequest): Promise<LeagueDto> {
  return apiFetch<LeagueDto>(`/api/admin/leagues/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}
