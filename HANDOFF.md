# Handoff Document — Soccer Prediction App

## Project Overview

A web app where friends predict soccer match scores, top goalscorers, and tournament winners. Users log in with Google, join leagues, and compete on a shared leaderboard. Admin manages leagues, teams, fixtures, and scoring rules.

- **Repo**: `dezsovarga/soccer-prediction` (private GitHub)
- **Backend**: `/backend` — Kotlin + Spring Boot 3.4.4 + Maven + PostgreSQL
- **Frontend**: `/frontend` — React 18 + TypeScript + Vite 8 + Tailwind CSS v4 + shadcn/ui v4
- **Spec**: `spec.md` — full requirements, data model (11 tables), API endpoints, milestones
- **Conventions**: `CLAUDE.md` — code style, testing rules, milestone implementation order
- **Dev**: `dev.sh` — starts PostgreSQL (Docker), backend, and frontend in one command

---

## Completed Milestones

### Milestone 1 — Auth & Project Skeleton ✅ (`515bfc7`, `5787c5e`, `452bce9`)

- Google OAuth2 login via Spring Security (session cookie, not JWT)
- `CustomOAuth2UserService` — creates/updates user on OAuth, assigns ADMIN role via `ADMIN_EMAIL` env var
- User entity, `/api/users/me` endpoint
- Frontend: protected routes, auth hooks, layout shell (navbar, admin sidebar)
- OAuth2 scope: `profile, email` (not `openid` — avoids OIDC bypass of custom user service)
- Callback URL pattern: `/api/auth/callback/{registrationId}`
- Vite proxy for `/api` → `http://localhost:8080` (cookie handling)

### Milestone 2 — Leagues & Join Flow ✅ (`292ad39`, `b40e79e`)

- League entity + LeagueMember + join code flow
- API-Football integration: search leagues, sync fixtures/standings/players
- Fixture, Standing, Player entities + DTOs + mappers
- Frontend: admin league creation, join league page, league view (fixtures + standings tabs)
- `SyncService` with `@Scheduled` polling

### Milestone 3 — Predictions & Scoring ✅ (`1ae2691`)

- Prediction entity + CRUD (editable until kickoff)
- TopScorerPick + LeagueWinnerPick entities
- `PredictionService.calculatePoints` — exact score / correct outcome / wrong
- Frontend: inline prediction inputs on fixtures, picks section (top scorer dropdown, league winner), "My Predictions" tab

### Milestone 4 — Manual League Management ✅ (`ed03039`)

This was the largest milestone. The free API-Football plan doesn't cover World Cup 2026, so we built a full manual management system.

**Key architecture decisions:**
- `LeagueMode` enum: `MANUAL` | `API_SYNCED` — gates all manual vs API logic
- Fixture keeps `homeTeam`/`awayTeam` string fields for display (zero frontend fixture rendering changes), plus optional Team FK references for manual leagues
- Standing computation: `StandingComputeService` rebuilds group standings from FINISHED fixtures on every result entry
- Result entry triggers: `PredictionService.calculatePoints` + `StandingComputeService.recomputeStandings`
- Team logos derived from country code via `https://flagcdn.com/w80/{code}.png`

**Backend (new/modified):**
- `team/` package — `Team.kt`, `TeamRepository.kt`, `TeamDto.kt`, `TeamService.kt`, `AdminTeamController.kt`
- `fixture/AdminFixtureService.kt` + `AdminFixtureController.kt` — fixture CRUD + result entry
- `standing/StandingComputeService.kt` — recomputes W/D/L/GF/GA/GD/Pts, groups by groupName, ranks by points→GD→GF
- Made nullable: `League.apiLeagueId`, `Fixture.apiFixtureId`, `Standing.apiTeamId`, `TopScorerPick.apiPlayerId`, `LeagueWinnerPick.apiTeamId`
- Added: `League.mode`, `Fixture.round`, `Standing.groupName`, `Standing.team` FK
- `SyncService.syncAll()` filters to `API_SYNCED` leagues only
- `LeagueService.createLeague` — validates mode, skips sync for MANUAL

**Frontend (new/modified):**
- `admin-leagues.tsx` — Manual/API-Synced toggle, mode badge column, Teams/Fixtures action links
- `admin-teams.tsx` — team CRUD with flag previews, group assignment
- `admin-fixtures.tsx` — fixture creation (team selects, kickoff, round), inline result entry
- `league-view.tsx` — grouped standings by groupName, free-text top scorer for manual leagues
- `hooks/use-admin-teams.ts`, `hooks/use-admin-fixtures.ts` — TanStack Query mutations with cache invalidation
- `types.ts` + `api.ts` — all new types and 9 new API functions

### Milestone 5 — Leaderboard & Admin Backoffice ✅

**Backend (new/modified):**
- `leaderboard/` package — `LeaderboardDto.kt`, `LeaderboardService.kt`, `LeaderboardController.kt`
  - `GET /api/leagues/{id}/leaderboard` — computes rankings at query time by summing `points_earned` across Prediction, TopScorerPick, LeagueWinnerPick per user per league
  - Ranking: total points → correct scores → correct outcomes → name (alphabetical tiebreak)
- `user/AdminUserController.kt` + `UserService.kt` — admin user management
  - `GET /api/admin/users` — list all users with createdAt
  - `PATCH /api/admin/users/{id}` — toggle is_active status
- `user/UserDto.kt` — added `AdminUserDto` (includes `createdAt`) + `UpdateUserRequest`
- Repository additions: `PredictionRepository.findByFixtureLeagueId`, `TopScorerPickRepository.findByLeagueId`, `LeagueWinnerPickRepository.findByLeagueId`, `LeagueMemberRepository.findByLeagueId`

**Frontend (new/modified):**
- `league-view.tsx` — new Leaderboard tab with ranked table (rank, player, exact scores, correct outcomes, bonus picks, total points)
- `admin-users.tsx` — full user management page with activate/deactivate toggle (disabled for admin users)
- `admin-leagues.tsx` — Edit button on each league → inline form to edit scoring settings (exact score pts, correct outcome pts, wrong prediction pts, top scorer bonus, winner bonus)
- `hooks/use-admin-users.ts` — `useAdminUsers()`, `useUpdateUser()`
- `hooks/use-admin-leagues.ts` — added `useUpdateLeague()`
- `hooks/use-leagues.ts` — added `useLeaderboard()`
- `types.ts` — added `LeaderboardEntryDto`, `AdminUserDto`, `UpdateUserRequest`, `UpdateLeagueRequest`
- `api.ts` — added `fetchLeaderboard`, `fetchAdminUsers`, `updateUser`, `updateLeagueSettings`
- `App.tsx` — replaced placeholder admin users route with `AdminUsersPage`
- `openapi.yaml` — version 0.5.0, added leaderboard endpoint, admin user endpoints, and all M5 schemas

---

## Current Test Counts

- **Backend**: 107 tests passing (`cd backend && ./mvnw test`)
- **Frontend**: 51 tests passing (`cd frontend && npm test -- --run`)

---

## All Design Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| League mode | `MANUAL` / `API_SYNCED` enum | Free API-Football doesn't cover World Cup 2026; manual is primary mode |
| Team logos | flagcdn.com via country code | Free, no API key needed, covers all countries |
| Fixture team display | Keep string fields, add optional Team FKs | Zero frontend fixture rendering changes needed |
| Standings computation | Recompute from FINISHED fixtures on result entry | Always accurate, delete-and-reinsert approach |
| Standings ranking | Points → Goal Difference → Goals For → Name | Standard football tiebreaker rules |
| Result entry flow | `PUT /api/admin/fixtures/{id}/result` → scoring → standings | Single endpoint triggers entire chain |
| Admin designation | `ADMIN_EMAIL` env var | Simplest, no seed script needed |
| Auth mechanism | Session cookie (JSESSIONID), not JWT | Simpler, Spring Security handles it |
| Auth flow | Backend-driven redirect | Frontend never touches OAuth tokens |
| Frontend auth state | TanStack Query on `/api/users/me` | 200=authenticated, 401=not |
| OAuth2 scope | `profile, email` (no `openid`) | `openid` triggers OIDC flow bypassing custom user service |
| API proxy | Vite dev server proxy for `/api` | Eliminates cross-origin cookie issues |
| Login URL | Direct to backend (not through Vite proxy) | OAuth redirect must not go through proxy |
| Build tool | Maven | User preference |
| Java version | 17 | Matches local JDK |
| Node version | 22 (via .nvmrc) | Required by Vite 8 / shadcn v4 |
| DB schema management | Hibernate `ddl-auto: update` | Good enough for dev; manual `ALTER TABLE` needed for NOT NULL → nullable changes on existing data |
| Testing — backend | JUnit 5 + MockK (unit), Spring Boot Test + H2 (integration) | Per CLAUDE.md |
| Testing — frontend | Vitest + React Testing Library | Per CLAUDE.md |

---

## Known Gotchas

1. **Hibernate `ddl-auto: update` doesn't drop NOT NULL constraints.** When making a column nullable (e.g., `api_league_id`), you must manually run `ALTER TABLE ... ALTER COLUMN ... DROP NOT NULL` on the existing PostgreSQL database. The Docker container DB is: `docker exec soccer-prediction-db psql -U postgres -d soccer_prediction -c "..."`.

2. **OAuth2 callback URL**: Google Cloud Console must have `http://localhost:8080/api/auth/callback/google` as authorized redirect URI.

3. **Frontend tests with duplicate text**: When a team name appears in both a `<select>` option and a table cell, use `getAllByText()` instead of `getByText()`.

---

## Where to Pick Up Next

### Milestone 6 — Polish & Deploy
- Loading states, error handling, empty states
- Dockerfiles, AWS deployment (ECS/Fargate, S3+CloudFront, RDS)

### Milestone 7 — API-Football Sync (Optional)
- Only needed if using paid API-Football plans

---

## Key Files Reference

| Purpose | Path |
|---------|------|
| Full spec | `spec.md` |
| Conventions | `CLAUDE.md` |
| OpenAPI spec | `backend/openapi.yaml` |
| Dev startup | `dev.sh` |
| Docker Compose | `docker-compose.yml` |
| Backend entry | `backend/src/main/kotlin/com/soccerprediction/SoccerPredictionApplication.kt` |
| Frontend entry | `frontend/src/App.tsx` |
| Frontend types | `frontend/src/lib/types.ts` |
| Frontend API | `frontend/src/lib/api.ts` |
