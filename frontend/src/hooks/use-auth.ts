import { useQuery } from '@tanstack/react-query';
import { apiFetch, ApiError } from '@/lib/api';
import type { UserDto } from '@/lib/types';

export function useAuth() {
  const { data: user, isLoading, error } = useQuery<UserDto>({
    queryKey: ['auth', 'me'],
    queryFn: () => apiFetch<UserDto>('/api/users/me'),
    retry: false,
    staleTime: 5 * 60 * 1000,
  });

  const isAuthenticated = !!user && !error;
  const isAdmin = user?.role === 'ADMIN';

  const isUnauthorized =
    error instanceof ApiError && error.status === 401;

  return { user, isLoading, isAuthenticated, isAdmin, isUnauthorized };
}
