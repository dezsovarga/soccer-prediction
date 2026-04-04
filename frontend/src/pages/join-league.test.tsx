import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { JoinLeaguePage } from './join-league';

vi.mock('@/hooks/use-leagues');

import { useJoinLeague } from '@/hooks/use-leagues';

const mockedUseJoinLeague = vi.mocked(useJoinLeague);

function renderWithRouter() {
  return render(
    <MemoryRouter>
      <JoinLeaguePage />
    </MemoryRouter>
  );
}

describe('JoinLeaguePage', () => {
  it('renders join code input and submit button', () => {
    mockedUseJoinLeague.mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      error: null,
    } as unknown as ReturnType<typeof useJoinLeague>);

    renderWithRouter();

    expect(screen.getByText('Join a League')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter the league join code')).toBeInTheDocument();
    expect(screen.getByText('Join League')).toBeInTheDocument();
  });

  it('disables button when join code is empty', () => {
    mockedUseJoinLeague.mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      error: null,
    } as unknown as ReturnType<typeof useJoinLeague>);

    renderWithRouter();

    expect(screen.getByText('Join League')).toBeDisabled();
  });

  it('shows pending state', () => {
    mockedUseJoinLeague.mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      error: null,
    } as unknown as ReturnType<typeof useJoinLeague>);

    renderWithRouter();

    expect(screen.getByText('Joining...')).toBeInTheDocument();
  });
});
