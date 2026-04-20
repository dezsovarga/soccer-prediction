import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { DashboardPage } from './dashboard';

vi.mock('@/hooks/use-auth');
vi.mock('@/hooks/use-leagues');
vi.mock('@/hooks/use-dashboard');

import { useAuth } from '@/hooks/use-auth';
import { useMyLeagues } from '@/hooks/use-leagues';
import { useDashboardData } from '@/hooks/use-dashboard';

const mockedUseAuth = vi.mocked(useAuth);
const mockedUseMyLeagues = vi.mocked(useMyLeagues);
const mockedUseDashboardData = vi.mocked(useDashboardData);

const defaultUser = {
  id: '1', email: 'test@example.com', displayName: 'John Doe',
  pictureUrl: null, role: 'USER', isActive: true,
};

const defaultAuth = {
  user: defaultUser,
  isLoading: false,
  isAuthenticated: true,
  isAdmin: false,
  isUnauthorized: false,
};

function setupMocks(overrides: {
  leagues?: ReturnType<typeof useMyLeagues>;
  dashboard?: ReturnType<typeof useDashboardData>;
  auth?: Partial<ReturnType<typeof useAuth>>;
} = {}) {
  mockedUseAuth.mockReturnValue({ ...defaultAuth, ...overrides.auth });
  mockedUseMyLeagues.mockReturnValue(
    overrides.leagues ?? { data: [], isLoading: false } as ReturnType<typeof useMyLeagues>
  );
  mockedUseDashboardData.mockReturnValue(
    overrides.dashboard ?? { upcoming: [], recent: [], leagueStats: [], isLoading: false }
  );
}

function renderWithRouter() {
  return render(
    <MemoryRouter>
      <DashboardPage />
    </MemoryRouter>
  );
}

describe('DashboardPage', () => {
  it('displays welcome message with user display name', () => {
    setupMocks();
    renderWithRouter();
    expect(screen.getByText('Welcome, John Doe')).toBeInTheDocument();
  });

  it('shows empty state when no leagues', () => {
    setupMocks({ auth: { user: { ...defaultUser, displayName: 'Jane' } } });
    renderWithRouter();
    expect(screen.getByText("You haven't joined any leagues yet.")).toBeInTheDocument();
  });

  it('shows leagues section with rank and points', () => {
    const league = { id: 'l1', name: 'Premier League', season: 2026, memberCount: 5 };
    setupMocks({
      leagues: { data: [league], isLoading: false } as ReturnType<typeof useMyLeagues>,
      dashboard: {
        upcoming: [],
        recent: [],
        leagueStats: [{ league, rank: 2, totalPoints: 15, upcomingCount: 3 }],
        isLoading: false,
      },
    });
    renderWithRouter();
    expect(screen.getByText('Premier League')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('15')).toBeInTheDocument();
    expect(screen.getByText('3 matches awaiting prediction')).toBeInTheDocument();
  });

  it('shows upcoming fixtures needing predictions', () => {
    const league = { id: 'l1', name: 'Premier League', season: 2026, memberCount: 5 };
    const fixture = {
      id: 'f1', apiFixtureId: null, homeTeam: 'Arsenal', awayTeam: 'Chelsea',
      homeTeamLogo: null, awayTeamLogo: null,
      kickoff: new Date(Date.now() + 86400000).toISOString(),
      homeScore: null, awayScore: null, status: 'SCHEDULED', round: null, matchday: 1,
    };
    setupMocks({
      leagues: { data: [league], isLoading: false } as ReturnType<typeof useMyLeagues>,
      dashboard: {
        upcoming: [{ fixture, leagueId: 'l1', leagueName: 'Premier League', predicted: false }],
        recent: [],
        leagueStats: [{ league, rank: null, totalPoints: 0, upcomingCount: 1 }],
        isLoading: false,
      },
    });
    renderWithRouter();
    expect(screen.getByText('Arsenal')).toBeInTheDocument();
    expect(screen.getByText('Chelsea')).toBeInTheDocument();
    expect(screen.getByText('Needs prediction')).toBeInTheDocument();
  });

  it('shows recent results with points earned', () => {
    const league = { id: 'l1', name: 'Premier League', season: 2026, memberCount: 5 };
    const prediction = {
      id: 'p1', fixtureId: 'f1', homeScore: 2, awayScore: 1, pointsEarned: 3,
      fixtureHomeTeam: 'Arsenal', fixtureAwayTeam: 'Chelsea',
      fixtureHomeTeamLogo: null, fixtureAwayTeamLogo: null,
      fixtureHomeScore: 2, fixtureAwayScore: 1,
      fixtureKickoff: new Date(Date.now() - 86400000).toISOString(),
      fixtureStatus: 'FINISHED', matchday: 1,
    };
    setupMocks({
      leagues: { data: [league], isLoading: false } as ReturnType<typeof useMyLeagues>,
      dashboard: {
        upcoming: [],
        recent: [{ prediction, leagueId: 'l1', leagueName: 'Premier League' }],
        leagueStats: [{ league, rank: 1, totalPoints: 3, upcomingCount: 0 }],
        isLoading: false,
      },
    });
    renderWithRouter();
    expect(screen.getByText('+3 pts')).toBeInTheDocument();
    expect(screen.getByText('Exact!')).toBeInTheDocument();
  });

  it('shows loading state', () => {
    setupMocks({
      leagues: { data: undefined, isLoading: true } as ReturnType<typeof useMyLeagues>,
    });
    renderWithRouter();
    expect(screen.getByText('Loading dashboard...')).toBeInTheDocument();
  });
});
