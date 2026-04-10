import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ToastProvider } from '@/components/toast';
import { AdminFixturesPage } from './admin-fixtures';

vi.mock('@/hooks/use-admin-teams');
vi.mock('@/hooks/use-admin-fixtures');

import { useTeams } from '@/hooks/use-admin-teams';
import { useAdminFixtures, useCreateFixture, useDeleteFixture, useEnterResult } from '@/hooks/use-admin-fixtures';

const mockedUseTeams = vi.mocked(useTeams);
const mockedUseAdminFixtures = vi.mocked(useAdminFixtures);
const mockedUseCreateFixture = vi.mocked(useCreateFixture);
const mockedUseDeleteFixture = vi.mocked(useDeleteFixture);
const mockedUseEnterResult = vi.mocked(useEnterResult);

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/admin/leagues/abc-123/fixtures']}>
      <ToastProvider>
        <Routes>
          <Route path="/admin/leagues/:id/fixtures" element={<AdminFixturesPage />} />
        </Routes>
      </ToastProvider>
    </MemoryRouter>
  );
}

describe('AdminFixturesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedUseCreateFixture.mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    } as unknown as ReturnType<typeof useCreateFixture>);
    mockedUseDeleteFixture.mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    } as unknown as ReturnType<typeof useDeleteFixture>);
    mockedUseEnterResult.mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    } as unknown as ReturnType<typeof useEnterResult>);
  });

  it('shows message when fewer than 2 teams', () => {
    mockedUseTeams.mockReturnValue({
      data: [{ id: '1', name: 'Brazil', countryCode: 'br', logoUrl: null, groupName: 'A' }],
      isLoading: false,
    } as ReturnType<typeof useTeams>);
    mockedUseAdminFixtures.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useAdminFixtures>);

    renderPage();

    expect(screen.getByText('Add at least 2 teams before creating fixtures.')).toBeInTheDocument();
  });

  it('shows fixtures in table', () => {
    mockedUseTeams.mockReturnValue({
      data: [
        { id: '1', name: 'Brazil', countryCode: 'br', logoUrl: null, groupName: 'A' },
        { id: '2', name: 'Germany', countryCode: 'de', logoUrl: null, groupName: 'A' },
      ],
      isLoading: false,
    } as ReturnType<typeof useTeams>);
    mockedUseAdminFixtures.mockReturnValue({
      data: [
        {
          id: 'fix-1',
          apiFixtureId: null,
          homeTeam: 'Brazil',
          awayTeam: 'Germany',
          homeTeamLogo: null,
          awayTeamLogo: null,
          kickoff: '2026-06-14T18:00:00Z',
          homeScore: null,
          awayScore: null,
          status: 'SCHEDULED',
          round: 'Group A',
          matchday: 1,
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useAdminFixtures>);

    renderPage();

    expect(screen.getAllByText('Brazil').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('Germany').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('Enter Result').length).toBeGreaterThanOrEqual(1);
  });

  it('shows empty state when no fixtures', () => {
    mockedUseTeams.mockReturnValue({
      data: [
        { id: '1', name: 'Brazil', countryCode: 'br', logoUrl: null, groupName: 'A' },
        { id: '2', name: 'Germany', countryCode: 'de', logoUrl: null, groupName: 'A' },
      ],
      isLoading: false,
    } as ReturnType<typeof useTeams>);
    mockedUseAdminFixtures.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useAdminFixtures>);

    renderPage();

    expect(screen.getByText('No fixtures created yet.')).toBeInTheDocument();
  });
});
