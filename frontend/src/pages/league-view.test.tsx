import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { LeagueViewPage } from './league-view';

vi.mock('@/hooks/use-leagues');
vi.mock('@/hooks/use-predictions');

import { useLeague, useFixtures, useStandings } from '@/hooks/use-leagues';
import {
  useMyPredictions,
  useSavePrediction,
  usePlayers,
  useTopScorerPick,
  useSaveTopScorerPick,
  useLeagueWinnerPick,
  useSaveLeagueWinnerPick,
} from '@/hooks/use-predictions';

const mockedUseLeague = vi.mocked(useLeague);
const mockedUseFixtures = vi.mocked(useFixtures);
const mockedUseStandings = vi.mocked(useStandings);
const mockedUseMyPredictions = vi.mocked(useMyPredictions);
const mockedUseSavePrediction = vi.mocked(useSavePrediction);
const mockedUsePlayers = vi.mocked(usePlayers);
const mockedUseTopScorerPick = vi.mocked(useTopScorerPick);
const mockedUseSaveTopScorerPick = vi.mocked(useSaveTopScorerPick);
const mockedUseLeagueWinnerPick = vi.mocked(useLeagueWinnerPick);
const mockedUseSaveLeagueWinnerPick = vi.mocked(useSaveLeagueWinnerPick);

const leagueData = {
  id: 'abc-123',
  name: 'Premier League',
  mode: 'API_SYNCED' as const,
  apiLeagueId: 39,
  season: 2026,
  joinCode: 'ABC',
  exactScorePoints: 3,
  correctOutcomePoints: 1,
  wrongPredictionPoints: 0,
  topScorerBonus: 10,
  leagueWinnerBonus: 10,
  memberCount: 5,
  createdAt: '2026-01-01T00:00:00Z',
};

const mutateMock = vi.fn();

function setupDefaultMocks() {
  mockedUseLeague.mockReturnValue({
    data: leagueData,
    isLoading: false,
  } as ReturnType<typeof useLeague>);
  mockedUseFixtures.mockReturnValue({
    data: [],
    isLoading: false,
  } as ReturnType<typeof useFixtures>);
  mockedUseStandings.mockReturnValue({
    data: [],
    isLoading: false,
  } as ReturnType<typeof useStandings>);
  mockedUseMyPredictions.mockReturnValue({
    data: [],
  } as ReturnType<typeof useMyPredictions>);
  mockedUseSavePrediction.mockReturnValue({
    mutate: mutateMock,
    isPending: false,
  } as unknown as ReturnType<typeof useSavePrediction>);
  mockedUsePlayers.mockReturnValue({
    data: [],
    isLoading: false,
  } as ReturnType<typeof usePlayers>);
  mockedUseTopScorerPick.mockReturnValue({
    data: undefined,
  } as ReturnType<typeof useTopScorerPick>);
  mockedUseSaveTopScorerPick.mockReturnValue({
    mutate: vi.fn(),
  } as unknown as ReturnType<typeof useSaveTopScorerPick>);
  mockedUseLeagueWinnerPick.mockReturnValue({
    data: undefined,
  } as ReturnType<typeof useLeagueWinnerPick>);
  mockedUseSaveLeagueWinnerPick.mockReturnValue({
    mutate: vi.fn(),
  } as unknown as ReturnType<typeof useSaveLeagueWinnerPick>);
}

function renderWithRouter() {
  return render(
    <MemoryRouter initialEntries={['/leagues/abc-123']}>
      <Routes>
        <Route path="/leagues/:id" element={<LeagueViewPage />} />
      </Routes>
    </MemoryRouter>
  );
}

describe('LeagueViewPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    setupDefaultMocks();
  });

  it('shows loading state', () => {
    mockedUseLeague.mockReturnValue({
      data: undefined,
      isLoading: true,
    } as ReturnType<typeof useLeague>);

    renderWithRouter();

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('shows league name and all tabs', () => {
    renderWithRouter();

    expect(screen.getByText('Premier League')).toBeInTheDocument();
    expect(screen.getByText('Fixtures')).toBeInTheDocument();
    expect(screen.getByText('My Predictions')).toBeInTheDocument();
    expect(screen.getByText('Picks')).toBeInTheDocument();
    expect(screen.getByText('Standings')).toBeInTheDocument();
  });

  it('shows empty fixtures message', () => {
    renderWithRouter();

    expect(screen.getByText('No fixtures yet. Data will appear after sync.')).toBeInTheDocument();
  });

  it('shows prediction inputs for scheduled fixtures', () => {
    const futureDate = new Date(Date.now() + 86400000).toISOString();
    mockedUseFixtures.mockReturnValue({
      data: [
        {
          id: 'fix-1',
          apiFixtureId: 100,
          homeTeam: 'Arsenal',
          awayTeam: 'Chelsea',
          homeTeamLogo: null,
          awayTeamLogo: null,
          kickoff: futureDate,
          homeScore: null,
          awayScore: null,
          status: 'SCHEDULED',
          matchday: 1,
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useFixtures>);

    renderWithRouter();

    expect(screen.getByText('Arsenal')).toBeInTheDocument();
    expect(screen.getByText('Chelsea')).toBeInTheDocument();
    expect(screen.getByLabelText(/Home score prediction/)).toBeInTheDocument();
    expect(screen.getByLabelText(/Away score prediction/)).toBeInTheDocument();
    expect(screen.getByText('Save')).toBeInTheDocument();
  });

  it('calls savePrediction when save is clicked', () => {
    const futureDate = new Date(Date.now() + 86400000).toISOString();
    mockedUseFixtures.mockReturnValue({
      data: [
        {
          id: 'fix-1',
          apiFixtureId: 100,
          homeTeam: 'Arsenal',
          awayTeam: 'Chelsea',
          homeTeamLogo: null,
          awayTeamLogo: null,
          kickoff: futureDate,
          homeScore: null,
          awayScore: null,
          status: 'SCHEDULED',
          matchday: 1,
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useFixtures>);

    renderWithRouter();

    const homeInput = screen.getByLabelText(/Home score prediction/);
    const awayInput = screen.getByLabelText(/Away score prediction/);

    fireEvent.change(homeInput, { target: { value: '2' } });
    fireEvent.change(awayInput, { target: { value: '1' } });
    fireEvent.click(screen.getByText('Save'));

    expect(mutateMock).toHaveBeenCalledWith({
      fixtureId: 'fix-1',
      request: { homeScore: 2, awayScore: 1 },
    });
  });

  it('shows existing prediction for finished fixture', () => {
    const pastDate = new Date(Date.now() - 86400000).toISOString();
    mockedUseFixtures.mockReturnValue({
      data: [
        {
          id: 'fix-1',
          apiFixtureId: 100,
          homeTeam: 'Arsenal',
          awayTeam: 'Chelsea',
          homeTeamLogo: null,
          awayTeamLogo: null,
          kickoff: pastDate,
          homeScore: 2,
          awayScore: 1,
          status: 'FINISHED',
          matchday: 1,
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useFixtures>);
    mockedUseMyPredictions.mockReturnValue({
      data: [
        {
          id: 'pred-1',
          fixtureId: 'fix-1',
          homeScore: 2,
          awayScore: 1,
          pointsEarned: 3,
          fixtureHomeTeam: 'Arsenal',
          fixtureAwayTeam: 'Chelsea',
          fixtureHomeScore: 2,
          fixtureAwayScore: 1,
          fixtureKickoff: pastDate,
          fixtureStatus: 'FINISHED',
          matchday: 1,
        },
      ],
    } as ReturnType<typeof useMyPredictions>);

    renderWithRouter();

    expect(screen.getByText('Your prediction:')).toBeInTheDocument();
    // "2 - 1" appears twice: actual score + prediction
    const scores = screen.getAllByText('2 - 1');
    expect(scores).toHaveLength(2);
    expect(screen.getByText('3 pts')).toBeInTheDocument();
  });

  it('does not show prediction inputs for past kickoff', () => {
    const pastDate = new Date(Date.now() - 86400000).toISOString();
    mockedUseFixtures.mockReturnValue({
      data: [
        {
          id: 'fix-1',
          apiFixtureId: 100,
          homeTeam: 'Arsenal',
          awayTeam: 'Chelsea',
          homeTeamLogo: null,
          awayTeamLogo: null,
          kickoff: pastDate,
          homeScore: null,
          awayScore: null,
          status: 'SCHEDULED',
          matchday: 1,
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useFixtures>);

    renderWithRouter();

    expect(screen.queryByLabelText(/Home score prediction/)).not.toBeInTheDocument();
  });

  it('shows my predictions table in predictions tab', () => {
    const pastDate = new Date(Date.now() - 86400000).toISOString();
    mockedUseMyPredictions.mockReturnValue({
      data: [
        {
          id: 'pred-1',
          fixtureId: 'fix-1',
          homeScore: 1,
          awayScore: 0,
          pointsEarned: 1,
          fixtureHomeTeam: 'Arsenal',
          fixtureAwayTeam: 'Chelsea',
          fixtureHomeScore: 3,
          fixtureAwayScore: 1,
          fixtureKickoff: pastDate,
          fixtureStatus: 'FINISHED',
          matchday: 1,
        },
      ],
    } as ReturnType<typeof useMyPredictions>);

    renderWithRouter();

    fireEvent.click(screen.getByText('My Predictions'));

    expect(screen.getByText('Arsenal vs Chelsea')).toBeInTheDocument();
    expect(screen.getByText('3 - 1')).toBeInTheDocument();
    expect(screen.getByText('1 - 0')).toBeInTheDocument();
  });
});
