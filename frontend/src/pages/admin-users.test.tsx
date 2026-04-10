import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ToastProvider } from '@/components/toast';
import { AdminUsersPage } from './admin-users';

vi.mock('@/hooks/use-admin-users');

import { useAdminUsers, useUpdateUser } from '@/hooks/use-admin-users';

const mockedUseAdminUsers = vi.mocked(useAdminUsers);
const mockedUseUpdateUser = vi.mocked(useUpdateUser);

const mutateMock = vi.fn();

function renderPage() {
  return render(
    <MemoryRouter>
      <ToastProvider>
        <AdminUsersPage />
      </ToastProvider>
    </MemoryRouter>
  );
}

describe('AdminUsersPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedUseUpdateUser.mockReturnValue({
      mutate: mutateMock,
      isPending: false,
    } as unknown as ReturnType<typeof useUpdateUser>);
  });

  it('shows loading state', () => {
    mockedUseAdminUsers.mockReturnValue({
      data: undefined,
      isLoading: true,
    } as ReturnType<typeof useAdminUsers>);

    renderPage();

    expect(screen.getByText('Loading users...')).toBeInTheDocument();
  });

  it('shows users in table', () => {
    mockedUseAdminUsers.mockReturnValue({
      data: [
        {
          id: 'u1',
          email: 'alice@test.com',
          displayName: 'Alice',
          pictureUrl: null,
          role: 'USER',
          isActive: true,
          createdAt: '2026-01-15T00:00:00Z',
        },
        {
          id: 'u2',
          email: 'admin@test.com',
          displayName: 'Admin',
          pictureUrl: null,
          role: 'ADMIN',
          isActive: true,
          createdAt: '2026-01-01T00:00:00Z',
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useAdminUsers>);

    renderPage();

    expect(screen.getByText('User Management')).toBeInTheDocument();
    expect(screen.getAllByText('Alice').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('alice@test.com').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('Admin').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('admin@test.com').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('Registered Users (2)')).toBeInTheDocument();
  });

  it('disables deactivate button for admin users', () => {
    mockedUseAdminUsers.mockReturnValue({
      data: [
        {
          id: 'u1',
          email: 'admin@test.com',
          displayName: 'Admin',
          pictureUrl: null,
          role: 'ADMIN',
          isActive: true,
          createdAt: '2026-01-01T00:00:00Z',
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useAdminUsers>);

    renderPage();

    const deactivateButtons = screen.getAllByText('Deactivate');
    expect(deactivateButtons[0]).toBeDisabled();
  });

  it('calls updateUser when deactivate is clicked', () => {
    mockedUseAdminUsers.mockReturnValue({
      data: [
        {
          id: 'u1',
          email: 'alice@test.com',
          displayName: 'Alice',
          pictureUrl: null,
          role: 'USER',
          isActive: true,
          createdAt: '2026-01-15T00:00:00Z',
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useAdminUsers>);

    renderPage();

    fireEvent.click(screen.getAllByText('Deactivate')[0]);

    expect(mutateMock).toHaveBeenCalledWith(
      { id: 'u1', request: { isActive: false } },
      expect.anything(),
    );
  });

  it('shows Activate button for inactive users', () => {
    mockedUseAdminUsers.mockReturnValue({
      data: [
        {
          id: 'u1',
          email: 'alice@test.com',
          displayName: 'Alice',
          pictureUrl: null,
          role: 'USER',
          isActive: false,
          createdAt: '2026-01-15T00:00:00Z',
        },
      ],
      isLoading: false,
    } as ReturnType<typeof useAdminUsers>);

    renderPage();

    expect(screen.getAllByText('Activate').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('Inactive').length).toBeGreaterThanOrEqual(1);
  });

  it('shows empty state when no users', () => {
    mockedUseAdminUsers.mockReturnValue({
      data: [],
      isLoading: false,
    } as ReturnType<typeof useAdminUsers>);

    renderPage();

    expect(screen.getByText('No users registered yet.')).toBeInTheDocument();
  });
});
