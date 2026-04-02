import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './protected-route';

vi.mock('@/hooks/use-auth');

import { useAuth } from '@/hooks/use-auth';

const mockedUseAuth = vi.mocked(useAuth);

describe('ProtectedRoute', () => {
  it('shows loading when auth is loading', () => {
    mockedUseAuth.mockReturnValue({
      user: undefined,
      isLoading: true,
      isAuthenticated: false,
      isAdmin: false,
      isUnauthorized: false,
    });

    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<div>Dashboard</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('redirects to login when not authenticated', () => {
    mockedUseAuth.mockReturnValue({
      user: undefined,
      isLoading: false,
      isAuthenticated: false,
      isAdmin: false,
      isUnauthorized: true,
    });

    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<div>Dashboard</div>} />
          </Route>
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  it('renders outlet when authenticated', () => {
    mockedUseAuth.mockReturnValue({
      user: { id: '1', email: 'test@example.com', displayName: 'Test', pictureUrl: null, role: 'USER', isActive: true },
      isLoading: false,
      isAuthenticated: true,
      isAdmin: false,
      isUnauthorized: false,
    });

    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<div>Dashboard</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
  });
});
