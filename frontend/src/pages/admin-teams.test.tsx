import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ToastProvider } from '@/components/toast';
import { AdminTeamsPage } from './admin-teams';

vi.mock('@/hooks/use-admin-teams');

import { useTeams, useCreateTeam, useDeleteTeam } from '@/hooks/use-admin-teams';

const mockedUseTeams = vi.mocked(useTeams);
const mockedUseCreateTeam = vi.mocked(useCreateTeam);
const mockedUseDeleteTeam = vi.mocked(useDeleteTeam);

const createMock = vi.fn();
const deleteMock = vi.fn();

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/admin/leagues/abc-123/teams']}>
      <ToastProvider>
        <Routes>
          <Route path="/admin/leagues/:id/teams" element={<AdminTeamsPage />} />
        </Routes>
      </ToastProvider>
    </MemoryRouter>
  );
}

describe('AdminTeamsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedUseCreateTeam.mockReturnValue({
      mutate: createMock,
      isPending: false,
    } as unknown as ReturnType<typeof useCreateTeam>);
    mockedUseDeleteTeam.mockReturnValue({
      mutate: deleteMock,
      isPending: false,
    } as unknown as ReturnType<typeof useDeleteTeam>);
  });

  it('shows empty state when no teams', () => {
    mockedUseTeams.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useTeams>);

    renderPage();

    expect(screen.getByText('No teams added yet.')).toBeInTheDocument();
  });

  it('shows teams in table', () => {
    mockedUseTeams.mockReturnValue({
      data: [
        { id: '1', name: 'Brazil', countryCode: 'br', logoUrl: 'https://flagcdn.com/w80/br.png', groupName: 'A' },
        { id: '2', name: 'Germany', countryCode: 'de', logoUrl: 'https://flagcdn.com/w80/de.png', groupName: 'A' },
      ],
      isLoading: false,
    } as ReturnType<typeof useTeams>);

    renderPage();

    // Teams appear in the table (may also appear in autocomplete suggestions)
    expect(screen.getAllByText('Brazil').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('Germany').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('Teams (2)')).toBeInTheDocument();
  });

  it('calls createTeam on form submit', () => {
    mockedUseTeams.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useTeams>);

    renderPage();

    fireEvent.change(screen.getByPlaceholderText('e.g. Brazil'), { target: { value: 'Brazil' } });
    fireEvent.change(screen.getByPlaceholderText('br'), { target: { value: 'br' } });
    fireEvent.change(screen.getByPlaceholderText('A'), { target: { value: 'A' } });
    fireEvent.click(screen.getByText('Add'));

    expect(createMock).toHaveBeenCalledWith(
      { name: 'Brazil', countryCode: 'br', groupName: 'A' },
      expect.anything()
    );
  });

  it('shows autocomplete suggestions when typing team name', () => {
    mockedUseTeams.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useTeams>);

    renderPage();

    const nameInput = screen.getByPlaceholderText('e.g. Brazil');
    fireEvent.focus(nameInput);
    fireEvent.change(nameInput, { target: { value: 'Bra' } });

    expect(screen.getByText('Brazil')).toBeInTheDocument();
  });

  it('selects team from autocomplete and fills code', () => {
    mockedUseTeams.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useTeams>);

    renderPage();

    const nameInput = screen.getByPlaceholderText('e.g. Brazil');
    fireEvent.focus(nameInput);
    fireEvent.change(nameInput, { target: { value: 'Ger' } });

    // Click the Germany suggestion
    fireEvent.click(screen.getByText('Germany'));

    expect(nameInput).toHaveValue('Germany');
    expect(screen.getByPlaceholderText('br')).toHaveValue('de');
  });
});
