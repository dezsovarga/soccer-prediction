import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { DashboardPage } from './dashboard';

vi.mock('@/hooks/use-auth');
vi.mock('@/hooks/use-leagues');

import { useAuth } from '@/hooks/use-auth';
import { useMyLeagues } from '@/hooks/use-leagues';

const mockedUseAuth = vi.mocked(useAuth);
const mockedUseMyLeagues = vi.mocked(useMyLeagues);

function renderWithRouter() {
  return render(
    <MemoryRouter>
      <DashboardPage />
    </MemoryRouter>
  );
}

describe('DashboardPage', () => {
  it('displays welcome message with user display name', () => {
    mockedUseAuth.mockReturnValue({
      user: { id: '1', email: 'test@example.com', displayName: 'John Doe', pictureUrl: null, role: 'USER', isActive: true },
      isLoading: false,
      isAuthenticated: true,
      isAdmin: false,
      isUnauthorized: false,
    });
    mockedUseMyLeagues.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useMyLeagues>);

    renderWithRouter();

    expect(screen.getByText('Welcome, John Doe')).toBeInTheDocument();
  });

  it('shows empty state when no leagues', () => {
    mockedUseAuth.mockReturnValue({
      user: { id: '1', email: 'test@example.com', displayName: 'Jane', pictureUrl: null, role: 'USER', isActive: true },
      isLoading: false,
      isAuthenticated: true,
      isAdmin: false,
      isUnauthorized: false,
    });
    mockedUseMyLeagues.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useMyLeagues>);

    renderWithRouter();

    expect(screen.getByText("You haven't joined any leagues yet.")).toBeInTheDocument();
  });

  it('shows leagues when user has joined leagues', () => {
    mockedUseAuth.mockReturnValue({
      user: { id: '1', email: 'test@example.com', displayName: 'Jane', pictureUrl: null, role: 'USER', isActive: true },
      isLoading: false,
      isAuthenticated: true,
      isAdmin: false,
      isUnauthorized: false,
    });
    mockedUseMyLeagues.mockReturnValue({
      data: [
        { id: 'l1', name: 'Premier League', season: 2026, memberCount: 5 },
      ],
      isLoading: false,
    } as ReturnType<typeof useMyLeagues>);

    renderWithRouter();

    expect(screen.getByText('Premier League')).toBeInTheDocument();
    expect(screen.getByText('5 members')).toBeInTheDocument();
  });

  it('shows loading state', () => {
    mockedUseAuth.mockReturnValue({
      user: { id: '1', email: 'test@example.com', displayName: 'Jane', pictureUrl: null, role: 'USER', isActive: true },
      isLoading: false,
      isAuthenticated: true,
      isAdmin: false,
      isUnauthorized: false,
    });
    mockedUseMyLeagues.mockReturnValue({
      data: undefined,
      isLoading: true,
    } as ReturnType<typeof useMyLeagues>);

    renderWithRouter();

    expect(screen.getByText('Loading leagues...')).toBeInTheDocument();
  });
});
