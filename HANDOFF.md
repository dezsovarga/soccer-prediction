# Handoff Document — Soccer Prediction App

## What Was Built

### Project Setup
- **Monorepo** at `dezsovarga/soccer-prediction` (private GitHub repo)
- **Backend**: `/backend` — Kotlin + Spring Boot 3.4.4 + Maven + PostgreSQL
- **Frontend**: `/frontend` — React 18 + TypeScript + Vite 8 + Tailwind CSS v4 + shadcn/ui v4
- **Spec**: `spec.md` at project root — full requirements, data model (10 tables), 16 API endpoints, 8 pages, 5 milestones
- **Conventions**: `CLAUDE.md` — code style, testing rules, shared conventions
- **Dev script**: `dev.sh` — starts PostgreSQL (Docker), backend, and frontend in one command

### Milestone 1 — Auth & Project Skeleton (COMPLETE, committed)

**Backend:**
- Google OAuth2 login via Spring Security
- `auth/SecurityConfig.kt` — CORS, CSRF disabled, OAuth2 login at `/api/auth/*`, 401 for unauthenticated, ADMIN role on `/api/admin/**`
- `auth/CustomOAuth2UserService.kt` — creates/updates user in DB during OAuth flow, assigns ADMIN role if email matches `ADMIN_EMAIL` env var
- `auth/OAuth2LoginSuccessHandler.kt` — redirects to frontend after OAuth
- `user/` — User entity, UserRepository, UserDto, UserMapper, UserController (`GET /api/users/me`)
- `common/` — ErrorResponse, GlobalExceptionHandler
- 19 tests committed (JUnit 5 + MockK + Spring Boot Test + H2)

**Frontend:**
- `lib/api.ts` — fetch wrapper with `credentials: "include"`, `ApiError` class
- `hooks/use-auth.ts` — TanStack Query hook for auth state
- `components/protected-route.tsx` + `admin-route.tsx` — route guards
- `components/layout/` — layout, admin-layout, navbar (avatar dropdown, logout), sidebar
- `pages/` — login, dashboard (placeholder), not-found
- shadcn/ui v4 components: button, avatar, dropdown-menu, separator
- 18 tests committed (Vitest + React Testing Library)

### Milestone 2 — Leagues, Fixtures & API-Football Sync (COMPLETE, uncommitted)

**Backend (new packages, all uncommitted):**
- `league/` — League + LeagueMember entities, LeagueRepository, LeagueMemberRepository, LeagueService, LeagueController (user endpoints), AdminLeagueController, DTOs + mappers
- `fixture/` — Fixture entity, FixtureRepository, FixtureDto, FixtureMapper
- `standing/` — Standing entity, StandingRepository, StandingDto, StandingMapper
- `player/` — Player entity, PlayerRepository
- `apifootball/` — ApiFootballConfig, ApiFootballModels, ApiFootballService (HTTP client for API-Football), SyncService (scheduled sync)
- League tests in `backend/src/test/kotlin/com/soccerprediction/league/`
- CustomOAuth2UserService test in `backend/src/test/kotlin/com/soccerprediction/auth/`
- `openapi.yaml` updated with all M2 endpoints
- **48 backend tests passing**

**Frontend (new files, all uncommitted):**
- `pages/admin-leagues.tsx` — admin league management (search API-Football, create league)
- `pages/join-league.tsx` — join league by code
- `pages/league-view.tsx` — fixture list + standings tabs
- `hooks/use-admin-leagues.ts`, `hooks/use-leagues.ts` — TanStack Query hooks
- `components/ui/` — badge, card, input, label, table, tabs (shadcn/ui)
- `App.tsx` updated with new routes
- `dashboard.tsx` updated to show user's leagues
- **30 frontend tests passing**

### OAuth2 Login Fixes (this session, uncommitted)

Fixed three bugs that prevented Google OAuth login from working:

1. **Vite proxy for API calls** (`frontend/vite.config.ts`)
   - Added `server.proxy` for `/api` → `http://localhost:8080` with `changeOrigin: true`
   - Changed `API_BASE` in `api.ts` to empty string (relative URLs go through proxy)
   - Login/logout URLs still point directly to `http://localhost:8080` via separate `BACKEND_URL` constant (OAuth flow must not go through proxy)

2. **OAuth2 callback URL mismatch** (`backend/src/main/resources/application.yml` + `SecurityConfig.kt`)
   - Changed `redirect-uri` from `{baseUrl}/api/auth/callback` to `{baseUrl}/api/auth/callback/{registrationId}` — Google now redirects to `/api/auth/callback/google`
   - Changed `redirectionEndpoint.baseUri` from `/api/auth/callback` to `/api/auth/callback/*` — Spring Security's `OAuth2LoginAuthenticationFilter` now matches the callback URL
   - **Google Cloud Console** authorized redirect URI updated to: `http://localhost:8080/api/auth/callback/google`

3. **OIDC vs OAuth2 user service** (`application.yml`)
   - Removed `openid` from OAuth2 scopes (now `scope: profile, email`)
   - With `openid`, Spring Security used `OidcUserService` instead of our `CustomOAuth2UserService`, so users were never created in the DB
   - Without `openid`, the plain OAuth2 flow calls our custom service correctly

4. **Removed silent error swallowing** (`CustomOAuth2UserService.kt`)
   - Removed try/catch that silently returned a valid OAuth2User when DB save failed — errors now propagate so OAuth login fails visibly

5. **Debug logging removed** — was temporarily enabled during debugging, removed after confirming login works

## Every Decision Made

| Decision | Choice | Reason |
|----------|--------|--------|
| Admin designation | `ADMIN_EMAIL` env var | Simplest, no seed script needed |
| Player picks (top scorer) | API-Football roster dropdown | Avoids typos/duplicates |
| Data sync | Scheduled backend cron job (`@Scheduled`) | Simpler than webhooks, predictable |
| Competitions per league | One leaderboard per league | Simpler, sufficient for friend groups |
| Leaderboard storage | Computed at query time (SUM) | No denormalized table, always accurate, fine for <50 users |
| Auth mechanism | Session cookie (JSESSIONID), not JWT | Simpler, Spring Security handles it, HttpOnly + SameSite=Lax |
| Auth flow | Backend-driven redirect | Frontend never touches OAuth tokens |
| Frontend auth state | Single TanStack Query on `/api/users/me` | 200=authenticated, 401=not, no local state |
| OAuth2 scope | `profile, email` (no `openid`) | `openid` triggers OIDC flow which bypasses custom OAuth2UserService |
| Callback URL pattern | `/api/auth/callback/{registrationId}` | Must match Spring Security's `OAuth2LoginAuthenticationFilter` wildcard pattern |
| API proxy | Vite dev server proxy for `/api` | Eliminates cross-origin cookie issues between ports 5173/8080 |
| Login URL | Direct to backend (`http://localhost:8080`) | OAuth full-page navigation must not go through Vite proxy (session cookie issues) |
| Build tool | Maven (was Gradle, migrated) | User preference |
| Java version | 17 target | Matches local JDK |
| Node version | 22 (via .nvmrc) | Required by Vite 8 / shadcn v4 |
| Testing — backend | JUnit 5 + MockK (unit), Spring Boot Test + H2 (integration) | Per CLAUDE.md |
| Testing — frontend | Vitest + React Testing Library | Per CLAUDE.md |
| Test location | Next to source files | Per CLAUDE.md |

## Current State

### Test results
- **Backend**: 48 tests passing (`cd backend && ./mvnw test`)
- **Frontend**: 30 tests passing (`cd frontend && npm test -- --run`)

### Login status
OAuth login flow **verified working** — user can sign in with Google and lands on the dashboard.

### Google Cloud Console
The authorized redirect URI is `http://localhost:8080/api/auth/callback/google`.

## Where to Pick Up

### Next milestone: Milestone 3 — Predictions
From `spec.md`, this includes:
- Users submit score predictions for upcoming fixtures
- Prediction entity + endpoints
- Prediction submission page in frontend
- Points calculation based on league scoring rules

### Key files to reference
- `spec.md` — full requirements, data model, API endpoints, milestones
- `CLAUDE.md` — code style, testing rules, milestone implementation order
- `backend/openapi.yaml` — API spec (source of truth)
