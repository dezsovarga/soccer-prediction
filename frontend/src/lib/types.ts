export type LeagueMode = 'MANUAL' | 'API_SYNCED';

export interface UserDto {
  id: string;
  email: string;
  displayName: string;
  pictureUrl: string | null;
  role: string;
  isActive: boolean;
}

export interface ErrorResponse {
  error: string;
  code: string;
  details?: unknown;
}

export interface LeagueDto {
  id: string;
  name: string;
  mode: LeagueMode;
  apiLeagueId: number | null;
  season: number;
  joinCode: string;
  exactScorePoints: number;
  correctOutcomePoints: number;
  wrongPredictionPoints: number;
  topScorerBonus: number;
  leagueWinnerBonus: number;
  memberCount: number;
  createdAt: string;
}

export interface LeagueSummaryDto {
  id: string;
  name: string;
  season: number;
  memberCount: number;
}

export interface TeamDto {
  id: string;
  name: string;
  countryCode: string | null;
  logoUrl: string | null;
  groupName: string | null;
}

export interface CreateTeamRequest {
  name: string;
  countryCode?: string;
  groupName?: string;
}

export interface UpdateTeamRequest {
  name?: string;
  countryCode?: string;
  groupName?: string;
}

export interface FixtureDto {
  id: string;
  apiFixtureId: number | null;
  homeTeam: string;
  awayTeam: string;
  homeTeamLogo: string | null;
  awayTeamLogo: string | null;
  kickoff: string;
  homeScore: number | null;
  awayScore: number | null;
  status: string;
  round: string | null;
  matchday: number;
}

export interface CreateFixtureRequest {
  homeTeamId: string;
  awayTeamId: string;
  kickoff: string;
  round?: string;
  matchday: number;
}

export interface UpdateFixtureRequest {
  homeTeamId?: string;
  awayTeamId?: string;
  kickoff?: string;
  round?: string;
  matchday?: number;
}

export interface EnterResultRequest {
  homeScore: number;
  awayScore: number;
}

export interface StandingDto {
  id: string;
  apiTeamId: number | null;
  teamName: string;
  teamLogo: string | null;
  groupName: string | null;
  rank: number;
  points: number;
  played: number;
  won: number;
  drawn: number;
  lost: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDiff: number;
}

export interface ApiFootballLeague {
  leagueId: number;
  name: string;
  country: string;
  logo: string | null;
  seasons: number[];
}

export interface CreateLeagueRequest {
  name: string;
  mode?: LeagueMode;
  apiLeagueId?: number;
  season: number;
  exactScorePoints?: number;
  correctOutcomePoints?: number;
  wrongPredictionPoints?: number;
  topScorerBonus?: number;
  leagueWinnerBonus?: number;
}

export interface JoinLeagueRequest {
  joinCode: string;
}

export interface PredictionDto {
  id: string;
  fixtureId: string;
  homeScore: number;
  awayScore: number;
  pointsEarned: number | null;
  fixtureHomeTeam: string;
  fixtureAwayTeam: string;
  fixtureHomeScore: number | null;
  fixtureAwayScore: number | null;
  fixtureKickoff: string;
  fixtureStatus: string;
  matchday: number;
}

export interface PredictionRequest {
  homeScore: number;
  awayScore: number;
}

export interface TopScorerPickDto {
  id: string;
  playerName: string;
  apiPlayerId: number | null;
  pointsEarned: number | null;
}

export interface TopScorerPickRequest {
  playerName: string;
  apiPlayerId?: number;
}

export interface LeagueWinnerPickDto {
  id: string;
  teamName: string;
  apiTeamId: number | null;
  pointsEarned: number | null;
}

export interface LeagueWinnerPickRequest {
  teamName: string;
  apiTeamId?: number;
}

export interface PlayerDto {
  apiPlayerId: number;
  name: string;
  photoUrl: string | null;
  position: string | null;
  apiTeamId: number;
}
