import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { LoginPage } from './login';

vi.mock('@/hooks/use-auth');

import { useAuth } from '@/hooks/use-auth';

const mockedUseAuth = vi.mocked(useAuth);

describe('LoginPage', () => {
  it('shows sign-in button when not authenticated', () => {
    mockedUseAuth.mockReturnValue({
      user: undefined,
      isLoading: false,
      isAuthenticated: false,
      isAdmin: false,
      isUnauthorized: true,
    });

    render(
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Sign in with Google')).toBeInTheDocument();
    expect(screen.getByText('Soccer Predictions')).toBeInTheDocument();
  });

  it('sign-in link points to correct oauth url', () => {
    mockedUseAuth.mockReturnValue({
      user: undefined,
      isLoading: false,
      isAuthenticated: false,
      isAdmin: false,
      isUnauthorized: true,
    });

    render(
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </MemoryRouter>
    );

    const link = screen.getByText('Sign in with Google');
    expect(link.closest('a')).toHaveAttribute(
      'href',
      'http://localhost:8080/api/auth/oauth2/authorize/google'
    );
  });

  it('shows deactivated error message from URL param', () => {
    mockedUseAuth.mockReturnValue({
      user: undefined,
      isLoading: false,
      isAuthenticated: false,
      isAdmin: false,
      isUnauthorized: true,
    });

    render(
      <MemoryRouter initialEntries={['/login?error=account_deactivated']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Your account has been deactivated. Please contact an administrator.')).toBeInTheDocument();
  });

  it('shows generic auth error message from URL param', () => {
    mockedUseAuth.mockReturnValue({
      user: undefined,
      isLoading: false,
      isAuthenticated: false,
      isAdmin: false,
      isUnauthorized: true,
    });

    render(
      <MemoryRouter initialEntries={['/login?error=auth_failed']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Authentication failed. Please try again.')).toBeInTheDocument();
  });

  it('redirects to home when already authenticated', () => {
    mockedUseAuth.mockReturnValue({
      user: { id: '1', email: 'test@example.com', displayName: 'Test', pictureUrl: null, role: 'USER', isActive: true },
      isLoading: false,
      isAuthenticated: true,
      isAdmin: false,
      isUnauthorized: false,
    });

    render(
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<div>Home Page</div>} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Home Page')).toBeInTheDocument();
  });
});
