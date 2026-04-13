# Handoff Document — Soccer Prediction App

## Project Overview

A web app where friends predict soccer match scores, top goalscorers, and tournament winners. Users log in with Google, join leagues, and compete on a shared leaderboard. Admin manages leagues, teams, fixtures, and scoring rules.

- **Repo**: `dezsovarga/soccer-prediction` (public GitHub)
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

### CI/CD Pipeline & Test Fixes ✅

**GitHub Actions CI pipeline (`.github/workflows/ci.yml`):**
- Triggers on push to `main` and PRs targeting `main`
- Two parallel jobs: Backend (Java 17, Maven, H2 in-memory tests) and Frontend (Node 22, lint, build, test)
- Maven and npm dependency caching for speed
- Concurrency: cancels in-progress runs on newer pushes to same branch/PR

**Test fixes for CI:**
- `AuthIntegrationTest` — fixed `@BeforeEach` cleanup to delete all tables in FK order (matching pattern of all other integration tests), preventing constraint violations from shared H2 context
- `eslint.config.js` — added `allowConstantExport: true` to `react-refresh/only-export-components` rule for shadcn/ui `cva` variant exports
- `tsconfig.app.json` — excluded `*.test.ts(x)` and `src/test/` from production `tsc` build (Vitest type-checks tests separately)
- `vite.config.ts` — added `/// <reference types="vitest/config" />` so TypeScript recognizes the `test` property
- `league-view.tsx` — typed fallback `new Map<number, FixtureDto[]>()` to fix implicit `any`
- **Frontend test updates** (all 5 test files): updated stale assertions after mobile optimization redesign — "Sign in with Google" → "Continue with Google", `getByText` → `getAllByText` for mobile/desktop duplicated elements, "My Predictions" tab → "Predictions"

### Prediction Team Logos ✅

- Added `fixtureHomeTeamLogo` and `fixtureAwayTeamLogo` to `PredictionDto` end-to-end: backend DTO, mapper, OpenAPI spec, frontend type, and both mobile/desktop predictions views
- Team flag images now rendered with rectangular aspect ratio (`object-contain`) consistently across fixtures, predictions, picks, and standings views

### Dark Mode ✅

- Tailwind CSS v4 dark mode via `.dark` class on `<html>` element (CSS variables already existed in `index.css`)
- `useTheme` hook (`frontend/src/hooks/use-theme.ts`) — manages dark/light state, persists to `localStorage`, respects system `prefers-color-scheme` on first visit
- Inline script in `index.html` applies stored theme before React mounts (prevents flash of wrong theme)
- Sun/Moon toggle button (lucide-react icons) in navbar and login page top-right corner
- `matchMedia` mock added to test setup for jsdom compatibility
- 4 unit tests for the `useTheme` hook

### FIFA World Cup 2026 Seed Data ✅

**Automatic seeding on backend startup:**
- `WorldCupDataSeeder` (`backend/src/main/kotlin/com/soccerprediction/common/WorldCupDataSeeder.kt`) — `ApplicationRunner` that checks for WC2026 league on startup; seeds if absent, skips if present
- `@Profile("!test")` — excluded from H2 test context (SQL uses PostgreSQL-specific `gen_random_uuid()`)
- SQL file: `backend/src/main/resources/db/seed-worldcup-2026.sql` — executed via Spring's `ScriptUtils.executeSqlScript()`

**Data seeded (join code: `WC2026`):**
- 1 league: "FIFA World Cup 2026" (mode: MANUAL, season: 2026)
- 48 teams across 12 groups (A-L), 4 per group, with country codes and flag logos from flagcdn.com
- 72 group stage fixtures with UTC kickoff times matching the official FIFA schedule (June 11-28, 2026)
- 48 standings entries initialized to zero
- Team logos propagated to fixtures (`home_team_logo`, `away_team_logo`) and standings (`team_logo`)

**Groups match the official December 2025 draw:**

| Group | Teams |
|-------|-------|
| A | Mexico, South Africa, South Korea, Czech Republic |
| B | Canada, Bosnia and Herzegovina, Qatar, Switzerland |
| C | Brazil, Morocco, Haiti, Scotland |
| D | United States, Paraguay, Australia, Turkey |
| E | Germany, Curaçao, Ivory Coast, Ecuador |
| F | Netherlands, Japan, Sweden, Tunisia |
| G | Belgium, Egypt, Iran, New Zealand |
| H | Spain, Cape Verde, Saudi Arabia, Uruguay |
| I | France, Senegal, Iraq, Norway |
| J | Argentina, Algeria, Austria, Jordan |
| K | Portugal, Congo DR, Uzbekistan, Colombia |
| L | England, Croatia, Ghana, Panama |

**Standalone shell script** (`scripts/seed-worldcup-2026.sh`) also available for manual seeding via `docker exec` into the PostgreSQL container.

---

## Current Test Counts

- **Backend**: 109 tests passing (`cd backend && ./mvnw test`)
- **Frontend**: 82 tests passing (`cd frontend && npm test -- --run`)

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
| Hosting | DigitalOcean Droplet (2GB, Frankfurt) | Cheapest option supporting Docker + Java |
| SSL termination | Caddy reverse proxy | Auto Let's Encrypt, zero config |
| Temporary domain | nip.io (`68-183-66-33.nip.io`) | Free, works with Google OAuth (requires real domain, not bare IP) |
| Container registry | GitHub Container Registry (GHCR) | Free for public repos, integrated with GitHub Actions |
| Deploy strategy | Build in CI → push to GHCR → pull on droplet | Avoids building on the small droplet |
| Reverse proxy chain | Caddy → Nginx (frontend) → Spring Boot (backend) | Caddy handles SSL, Nginx serves SPA + proxies `/api/`, backend handles business logic |
| Dark mode | Tailwind `.dark` class + `localStorage` + system preference | Zero-dependency, no flash on reload via inline `<script>` |
| WC2026 seed data | `ApplicationRunner` + `ScriptUtils` on startup | Automatic, idempotent (checks `join_code='WC2026'`), excluded from tests via `@Profile("!test")` |
| Seed SQL style | Plain INSERT/UPDATE, no `DO $$` blocks | Compatible with Spring's `ScriptUtils` semicolon-based statement parsing |

---

## Known Gotchas

1. **Hibernate `ddl-auto: update` doesn't drop NOT NULL constraints.** When making a column nullable (e.g., `api_league_id`), you must manually run `ALTER TABLE ... ALTER COLUMN ... DROP NOT NULL` on the existing PostgreSQL database. The Docker container DB is: `docker exec soccer-prediction-db psql -U postgres -d soccer_prediction -c "..."`.

2. **OAuth2 callback URL**: Google Cloud Console must have `http://localhost:8080/api/auth/callback/google` (local dev) and `https://68-183-66-33.nip.io/api/auth/callback/google` (production) as authorized redirect URIs. Google OAuth does not allow bare IP addresses as origins — a domain (even nip.io) is required.

4. **Forward headers in production**: `server.forward-headers-strategy: framework` is critical — without it, Spring generates `http://` OAuth redirect URIs behind the HTTPS Caddy proxy, causing Google OAuth to fail.

5. **Nginx must forward `X-Forwarded-*` headers**: The frontend Nginx uses `$http_x_forwarded_proto` (not `$scheme`) to pass through Caddy's headers to the backend. Using `$scheme` would reset them to `http` since Nginx-to-backend is plain HTTP.

3. **Frontend tests with duplicate text**: When a team name appears in both a `<select>` option and a table cell, use `getAllByText()` instead of `getByText()`.

### Milestone 6 (partial) — UI Polish & Mobile Optimization ✅

**Login page redesign:**
- Desktop: split layout — dark navy blue (`oklch(0.25 0.08 260)`) left panel with hero tagline ("Predict. Compete. Win."), feature highlights, and copyright; white right panel with login card
- Mobile: full dark navy blue background, frosted-glass card (`bg-white/10 backdrop-blur-sm`), white text, feature list below card
- Google button: solid white with official multi-color Google "G" icon, works on both dark/light backgrounds
- Error messages styled in `bg-destructive/10` banner

**Admin pages — mobile-responsive layout:**
- Collapsible sidebar: hidden off-screen on mobile with hamburger menu in navbar, slides in as overlay with dark backdrop, auto-closes on navigation; opaque `bg-background` on mobile, subtle `bg-muted/40` on desktop
- All admin tables (users, leagues, teams, fixtures) replaced with dual layout: stacked card list on mobile (`md:hidden`), original table on desktop (`hidden md:block`)
- Admin forms (teams, fixtures) stack vertically on mobile (`flex-col md:flex-row`), fixture team selects use responsive grid (`grid-cols-1 sm:grid-cols-2`)
- Page header links wrap with `flex-wrap`

**Admin teams — inline group editing:**
- Edit button on each team row (both desktop table and mobile cards)
- Clicking Edit shows inline group name input with Save/Cancel
- Uses existing `useUpdateTeam` hook — no backend changes for this feature

**League view (prediction page) — mobile optimization:**
- Fixture cards: redesigned with 3-column CSS grid (`grid-cols-[1fr_auto_1fr]`) — home team | score/time | away team; status badge on separate row; prediction input separated with border divider
- Tabs: horizontally scrollable on mobile via `overflow-x-auto` wrapper; shortened "My Predictions" to "Predictions"
- My Predictions tab: card list on mobile showing match + matchday, score vs prediction, points badge
- Leaderboard tab: compact cards on mobile with rank, avatar, name, stats summary, bold total points
- Standings tab: horizontal scroll with `min-w-[540px]`; column order prioritizes P/GD/Pts after Team; `table-fixed` with `<colgroup>` for consistent column widths across groups; team names truncate with `...` and `title` tooltip
- Page header: `flex-wrap` and responsive title size (`text-xl md:text-2xl`)

**Backend — standings on team changes:**
- `TeamService` now calls `StandingComputeService.recomputeStandings()` after every team create, update, and delete
- Standings are populated with zeroed stats as soon as teams are added (before any fixture results)
- The Standings tab in the user's league view shows all teams immediately
- `TeamServiceTest` updated with mocked `StandingComputeService`

---

## Where to Pick Up Next

- **Custom domain**: Buy a domain, point DNS to `68.183.66.33`, update `DOMAIN` + `FRONTEND_URL` in `.env.production`, update Google OAuth URIs. Caddy auto-provisions SSL.
- **Milestone 7**: API-Football sync (only if using paid plans)

---

### Milestone 6 (remaining) — Production Deployment ✅

**Infrastructure (DigitalOcean):**
- Droplet: Ubuntu 24.04, 2GB RAM, Frankfurt (`fra1`), IP `68.183.66.33`
- Firewall: only ports 22 (SSH), 80 (HTTP), 443 (HTTPS) open
- Domain: `68-183-66-33.nip.io` via nip.io (free DNS, maps to droplet IP — no purchased domain yet)
- SSL: automatic via Caddy + Let's Encrypt

**Docker setup:**
- `backend/Dockerfile` — multi-stage build: JDK 17 compile → JRE 17 runtime
- `frontend/Dockerfile` — multi-stage build: Node 22 `npm run build` → Nginx serving static files
- `frontend/nginx.conf` — serves SPA (fallback to `index.html`), proxies `/api/` to backend container, forwards `X-Forwarded-*` headers from Caddy
- `docker-compose.prod.yml` — 4 services: `postgres` (16-alpine), `backend`, `frontend`, `caddy` (2-alpine for HTTPS termination)
- `caddy/Caddyfile` — reverse proxies to frontend container, auto-provisions SSL certificates
- `.env.production.example` — template for secrets (DB password, Google OAuth, API-Football key, domain)
- `backend/.dockerignore`, `frontend/.dockerignore` — keep build contexts clean

**Backend changes for production:**
- `application.yml` — added `server.forward-headers-strategy: framework` (needed for correct OAuth redirect URIs behind reverse proxy)
- `application.yml` — added `server.servlet.session.cookie.secure: ${SESSION_COOKIE_SECURE:false}` (enabled in production compose)

**CI/CD pipeline (`.github/workflows/deploy.yml`):**
- Triggers: on successful CI workflow completion on `main` branch
- Build: uses Docker Buildx with GitHub Actions cache (`type=gha`) for fast rebuilds
- Push: images to GitHub Container Registry (`ghcr.io/dezsovarga/soccer-prediction/backend:latest` and `/frontend:latest`), tagged with both `latest` and commit SHA
- Deploy: SCPs `docker-compose.prod.yml` + `caddy/Caddyfile` to droplet, then SSHs in to pull new images and restart containers
- No GHCR auth needed on droplet (repo is public → images are public)
- Required GitHub secrets: `DROPLET_IP`, `SSH_PRIVATE_KEY`, `DOMAIN`

**Server setup:**
- `scripts/server-setup.sh` — one-time droplet provisioning script (installs Docker, creates app directory, templates `.env.production`)
- App deployed to `/opt/soccer-prediction/` on the droplet
- First deploy was done by copying source + building on droplet; subsequent deploys pull pre-built GHCR images

**Other changes:**
- `.gitignore` — added `.env.production`
- Repo visibility changed from private to public (simplifies GHCR image pulling)
- `doctl` CLI installed locally for managing DigitalOcean resources

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
| Docker Compose (dev) | `docker-compose.yml` |
| Docker Compose (prod) | `docker-compose.prod.yml` |
| Backend Dockerfile | `backend/Dockerfile` |
| Frontend Dockerfile | `frontend/Dockerfile` |
| Frontend Nginx config | `frontend/nginx.conf` |
| Caddy config | `caddy/Caddyfile` |
| Env template (prod) | `.env.production.example` |
| Server setup script | `scripts/server-setup.sh` |
| CI pipeline | `.github/workflows/ci.yml` |
| Deploy pipeline | `.github/workflows/deploy.yml` |
| Backend entry | `backend/src/main/kotlin/com/soccerprediction/SoccerPredictionApplication.kt` |
| Frontend entry | `frontend/src/App.tsx` |
| Frontend types | `frontend/src/lib/types.ts` |
| Frontend API | `frontend/src/lib/api.ts` |
| Dark mode hook | `frontend/src/hooks/use-theme.ts` |
| WC2026 data seeder | `backend/src/main/kotlin/com/soccerprediction/common/WorldCupDataSeeder.kt` |
| WC2026 seed SQL | `backend/src/main/resources/db/seed-worldcup-2026.sql` |
| WC2026 shell script | `scripts/seed-worldcup-2026.sh` |
