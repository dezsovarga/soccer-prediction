import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { LeagueViewPage } from './league-view';

vi.mock('@/hooks/use-leagues');

import { useLeague, useFixtures, useStandings } from '@/hooks/use-leagues';

const mockedUseLeague = vi.mocked(useLeague);
const mockedUseFixtures = vi.mocked(useFixtures);
const mockedUseStandings = vi.mocked(useStandings);

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
  it('shows loading state', () => {
    mockedUseLeague.mockReturnValue({
      data: undefined,
      isLoading: true,
    } as ReturnType<typeof useLeague>);
    mockedUseFixtures.mockReturnValue({
      data: undefined,
      isLoading: true,
    } as ReturnType<typeof useFixtures>);
    mockedUseStandings.mockReturnValue({
      data: undefined,
      isLoading: true,
    } as ReturnType<typeof useStandings>);

    renderWithRouter();

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('shows league name and tabs', () => {
    mockedUseLeague.mockReturnValue({
      data: {
        id: 'abc-123',
        name: 'Premier League',
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
      },
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

    renderWithRouter();

    expect(screen.getByText('Premier League')).toBeInTheDocument();
    expect(screen.getByText('Fixtures')).toBeInTheDocument();
    expect(screen.getByText('Standings')).toBeInTheDocument();
  });

  it('shows empty fixtures message', () => {
    mockedUseLeague.mockReturnValue({
      data: {
        id: 'abc-123',
        name: 'PL',
        apiLeagueId: 39,
        season: 2026,
        joinCode: 'ABC',
        exactScorePoints: 3,
        correctOutcomePoints: 1,
        wrongPredictionPoints: 0,
        topScorerBonus: 10,
        leagueWinnerBonus: 10,
        memberCount: 1,
        createdAt: '2026-01-01T00:00:00Z',
      },
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

    renderWithRouter();

    expect(screen.getByText('No fixtures yet. Data will appear after sync.')).toBeInTheDocument();
  });
});
