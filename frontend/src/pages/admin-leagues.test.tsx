import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AdminLeaguesPage } from './admin-leagues';

vi.mock('@/hooks/use-admin-leagues');

import { useAdminLeagues, useSearchApiFootballLeagues, useCreateLeague } from '@/hooks/use-admin-leagues';

const mockedUseAdminLeagues = vi.mocked(useAdminLeagues);
const mockedUseSearchApiFootballLeagues = vi.mocked(useSearchApiFootballLeagues);
const mockedUseCreateLeague = vi.mocked(useCreateLeague);

function renderWithRouter() {
  return render(
    <MemoryRouter>
      <AdminLeaguesPage />
    </MemoryRouter>
  );
}

describe('AdminLeaguesPage', () => {
  beforeEach(() => {
    mockedUseSearchApiFootballLeagues.mockReturnValue({
      data: undefined,
      isLoading: false,
    } as ReturnType<typeof useSearchApiFootballLeagues>);
    mockedUseCreateLeague.mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    } as unknown as ReturnType<typeof useCreateLeague>);
  });

  it('shows page title and search input', () => {
    mockedUseAdminLeagues.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useAdminLeagues>);

    renderWithRouter();

    expect(screen.getByText('League Management')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('e.g. Premier League, La Liga...')).toBeInTheDocument();
  });

  it('shows existing leagues in table', () => {
    mockedUseAdminLeagues.mockReturnValue({
      data: [
        {
          id: 'l1',
          name: 'Premier League',
          apiLeagueId: 39,
          season: 2026,
          joinCode: 'ABC12345',
          exactScorePoints: 3,
          correctOutcomePoints: 1,
          wrongPredictionPoints: 0,
          topScorerBonus: 10,
          leagueWinnerBonus: 10,
          memberCount: 3,
          createdAt: '2026-01-01T00:00:00Z',
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useAdminLeagues>);

    renderWithRouter();

    expect(screen.getByText('Premier League')).toBeInTheDocument();
    expect(screen.getByText('ABC12345')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
  });

  it('shows empty state when no leagues exist', () => {
    mockedUseAdminLeagues.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useAdminLeagues>);

    renderWithRouter();

    expect(screen.getByText('No leagues created yet.')).toBeInTheDocument();
  });
});
