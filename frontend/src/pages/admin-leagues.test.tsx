import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ToastProvider } from '@/components/toast';
import { AdminLeaguesPage } from './admin-leagues';

vi.mock('@/hooks/use-admin-leagues');

import { useAdminLeagues, useSearchApiFootballLeagues, useCreateLeague, useUpdateLeague } from '@/hooks/use-admin-leagues';

const mockedUseAdminLeagues = vi.mocked(useAdminLeagues);
const mockedUseSearchApiFootballLeagues = vi.mocked(useSearchApiFootballLeagues);
const mockedUseCreateLeague = vi.mocked(useCreateLeague);
const mockedUseUpdateLeague = vi.mocked(useUpdateLeague);

function renderWithRouter() {
  return render(
    <MemoryRouter>
      <ToastProvider>
        <AdminLeaguesPage />
      </ToastProvider>
    </MemoryRouter>
  );
}

describe('AdminLeaguesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedUseSearchApiFootballLeagues.mockReturnValue({
      data: undefined,
      isLoading: false,
    } as ReturnType<typeof useSearchApiFootballLeagues>);
    mockedUseCreateLeague.mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    } as unknown as ReturnType<typeof useCreateLeague>);
    mockedUseUpdateLeague.mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    } as unknown as ReturnType<typeof useUpdateLeague>);
  });

  it('shows page title and manual creation form by default', () => {
    mockedUseAdminLeagues.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useAdminLeagues>);

    renderWithRouter();

    expect(screen.getByText('League Management')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('e.g. World Cup 2026')).toBeInTheDocument();
    expect(screen.getByText('Create Manual League')).toBeInTheDocument();
  });

  it('shows existing leagues in table with mode badge', () => {
    mockedUseAdminLeagues.mockReturnValue({
      data: [
        {
          id: 'l1',
          name: 'Premier League',
          mode: 'API_SYNCED',
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

    expect(screen.getAllByText('Premier League').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('ABC12345').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('API').length).toBeGreaterThanOrEqual(1);
  });

  it('shows Teams/Fixtures links for manual leagues', () => {
    mockedUseAdminLeagues.mockReturnValue({
      data: [
        {
          id: 'l1',
          name: 'World Cup 2026',
          mode: 'MANUAL',
          apiLeagueId: null,
          season: 2026,
          joinCode: 'WC2026',
          exactScorePoints: 3,
          correctOutcomePoints: 1,
          wrongPredictionPoints: 0,
          topScorerBonus: 10,
          leagueWinnerBonus: 10,
          memberCount: 5,
          createdAt: '2026-01-01T00:00:00Z',
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useAdminLeagues>);

    renderWithRouter();

    expect(screen.getAllByText('Teams').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('Fixtures').length).toBeGreaterThanOrEqual(1);
  });

  it('shows Edit button that opens edit form', () => {
    mockedUseAdminLeagues.mockReturnValue({
      data: [
        {
          id: 'l1',
          name: 'World Cup 2026',
          mode: 'MANUAL',
          apiLeagueId: null,
          season: 2026,
          joinCode: 'WC2026',
          exactScorePoints: 3,
          correctOutcomePoints: 1,
          wrongPredictionPoints: 0,
          topScorerBonus: 10,
          leagueWinnerBonus: 10,
          memberCount: 5,
          createdAt: '2026-01-01T00:00:00Z',
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useAdminLeagues>);

    renderWithRouter();

    fireEvent.click(screen.getAllByText('Edit')[0]);

    expect(screen.getByText('Edit League: World Cup 2026')).toBeInTheDocument();
    expect(screen.getByLabelText('Exact Score Pts')).toHaveValue(3);
    expect(screen.getByLabelText('Correct Outcome Pts')).toHaveValue(1);
    expect(screen.getByText('Save Changes')).toBeInTheDocument();
    expect(screen.getByText('Cancel')).toBeInTheDocument();
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
