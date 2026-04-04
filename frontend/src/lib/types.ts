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
  apiLeagueId: number;
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

export interface FixtureDto {
  id: string;
  apiFixtureId: number;
  homeTeam: string;
  awayTeam: string;
  homeTeamLogo: string | null;
  awayTeamLogo: string | null;
  kickoff: string;
  homeScore: number | null;
  awayScore: number | null;
  status: string;
  matchday: number;
}

export interface StandingDto {
  id: string;
  apiTeamId: number;
  teamName: string;
  teamLogo: string | null;
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
  apiLeagueId: number;
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
