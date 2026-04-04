import { describe, it, expect, vi, beforeEach } from 'vitest';
import { apiFetch, ApiError, getLoginUrl, getLogoutUrl } from './api';

describe('apiFetch', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('sends request with credentials and json headers', async () => {
    const mockResponse = { id: '1', email: 'test@example.com' };
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify(mockResponse), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    await apiFetch('/api/users/me');

    expect(fetch).toHaveBeenCalledWith(
      '/api/users/me',
      expect.objectContaining({
        credentials: 'include',
        headers: expect.objectContaining({
          'Content-Type': 'application/json',
        }),
      })
    );
  });

  it('returns parsed json on success', async () => {
    const mockResponse = { id: '1', email: 'test@example.com' };
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify(mockResponse), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    const result = await apiFetch('/api/users/me');

    expect(result).toEqual(mockResponse);
  });

  it('throws ApiError on non-ok response', async () => {
    const errorBody = { error: 'Unauthorized', code: 'UNAUTHORIZED' };
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify(errorBody), {
        status: 401,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    try {
      await apiFetch('/api/users/me');
      expect.fail('Should have thrown');
    } catch (e) {
      expect(e).toBeInstanceOf(ApiError);
      expect((e as ApiError).status).toBe(401);
      expect((e as ApiError).body.code).toBe('UNAUTHORIZED');
    }
  });
});

describe('getLoginUrl', () => {
  it('returns the google oauth2 authorization url', () => {
    expect(getLoginUrl()).toBe('http://localhost:8080/api/auth/oauth2/authorize/google');
  });
});

describe('getLogoutUrl', () => {
  it('returns the logout url', () => {
    expect(getLogoutUrl()).toBe('http://localhost:8080/api/auth/logout');
  });
});
