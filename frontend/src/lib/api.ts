import type { ErrorResponse } from './types';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export class ApiError extends Error {
  status: number;
  body: ErrorResponse;

  constructor(status: number, body: ErrorResponse) {
    super(body.error);
    this.status = status;
    this.body = body;
  }
}

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  if (!res.ok) {
    const body = await res.json().catch(() => ({
      error: 'Unknown error',
      code: 'UNKNOWN',
    }));
    throw new ApiError(res.status, body);
  }

  return res.json();
}

export function getLoginUrl(): string {
  return `${API_BASE}/oauth2/authorization/google`;
}

export function getLogoutUrl(): string {
  return `${API_BASE}/api/auth/logout`;
}
