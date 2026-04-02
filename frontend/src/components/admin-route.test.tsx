import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AdminRoute } from './admin-route';

vi.mock('@/hooks/use-auth');

import { useAuth } from '@/hooks/use-auth';

const mockedUseAuth = vi.mocked(useAuth);

describe('AdminRoute', () => {
  it('redirects to login when not authenticated', () => {
    mockedUseAuth.mockReturnValue({
      user: undefined,
      isLoading: false,
      isAuthenticated: false,
      isAdmin: false,
      isUnauthorized: true,
    });

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route element={<AdminRoute />}>
            <Route path="/admin" element={<div>Admin Panel</div>} />
          </Route>
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  it('redirects to home when authenticated but not admin', () => {
    mockedUseAuth.mockReturnValue({
      user: { id: '1', email: 'test@example.com', displayName: 'Test', pictureUrl: null, role: 'USER', isActive: true },
      isLoading: false,
      isAuthenticated: true,
      isAdmin: false,
      isUnauthorized: false,
    });

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route element={<AdminRoute />}>
            <Route path="/admin" element={<div>Admin Panel</div>} />
          </Route>
          <Route path="/" element={<div>Home Page</div>} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Home Page')).toBeInTheDocument();
  });

  it('renders outlet when admin', () => {
    mockedUseAuth.mockReturnValue({
      user: { id: '1', email: 'admin@example.com', displayName: 'Admin', pictureUrl: null, role: 'ADMIN', isActive: true },
      isLoading: false,
      isAuthenticated: true,
      isAdmin: true,
      isUnauthorized: false,
    });

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route element={<AdminRoute />}>
            <Route path="/admin" element={<div>Admin Panel</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Admin Panel')).toBeInTheDocument();
  });
});
