import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { DashboardPage } from './dashboard';

vi.mock('@/hooks/use-auth');

import { useAuth } from '@/hooks/use-auth';

const mockedUseAuth = vi.mocked(useAuth);

describe('DashboardPage', () => {
  it('displays welcome message with user display name', () => {
    mockedUseAuth.mockReturnValue({
      user: { id: '1', email: 'test@example.com', displayName: 'John Doe', pictureUrl: null, role: 'USER', isActive: true },
      isLoading: false,
      isAuthenticated: true,
      isAdmin: false,
      isUnauthorized: false,
    });

    render(<DashboardPage />);

    expect(screen.getByText('Welcome, John Doe')).toBeInTheDocument();
  });
});
