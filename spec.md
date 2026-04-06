# Soccer Prediction App — Spec

## Overview

A web app where a group of friends predict soccer match scores, top goalscorers, and tournament/league winners. Users log in with Google, join leagues, and compete on a shared leaderboard. An admin manages users, leagues, teams, fixtures, and scoring rules. Leagues can be manually managed (primary mode) or optionally synced from API-Football.

---

## Tech Stack

| Layer       | Technology                                      |
|-------------|--------------------------------------------------|
| Frontend    | React 18, TypeScript, Vite, Tailwind CSS, shadcn/ui, TanStack Query, React Router |
| Backend     | Kotlin, Spring Boot 3, Spring Security (OAuth2), Spring Data JPA |
| Database    | PostgreSQL                                       |
| External    | API-Football (optional, via RapidAPI) for fixtures, results, squads |
| Auth        | Google OAuth 2.0                                 |
| Hosting     | AWS (ECS/Fargate for backend, S3 + CloudFront for frontend, RDS for PostgreSQL) |

---

## Core Features

### Auth & Users
- Google OAuth login. First login creates the user record.
- Admin role is granted to the user whose email matches the `ADMIN_EMAIL` env variable.
- Users have a display name and profile picture (pulled from Google).

### Leagues
- A league has a **mode**: `MANUAL` or `API_SYNCED`.
- **Manual mode** (primary): admin creates teams, groups, fixtures, and enters match results by hand. Designed for tournaments like the World Cup where free API data may not be available.
- **API-synced mode** (optional): admin selects a football league/season from API-Football and data is synced automatically.
- Each league has a unique join code. Users enter the code to join.
- Users can join multiple leagues.
- One shared leaderboard per league — all members compete together.

### Teams
- In manual mode, admin adds teams with a name and country code. Team logos are derived from country flags via `flagcdn.com` (e.g., `https://flagcdn.com/w80/br.png` for Brazil).
- In API-synced mode, teams are populated from API-Football data.
- Teams can be organized into groups (e.g., Group A–L for World Cup).

### Fixtures
- In manual mode, admin creates fixtures by selecting two teams, a matchday/round, and a kickoff time. Admin enters results manually after matches finish.
- In API-synced mode, fixtures are synced automatically.
- Fixtures have a round label (e.g., "Group A", "Round of 16", "Quarter-final", "Semi-final", "Final") and a matchday number for ordering.

### Predictions
- **Match score predictions**: users predict the exact score (e.g., 2-1) for each fixture.
- **Top goalscorer prediction**: users pick one player (manual text entry or dropdown from roster). One pick per league, changeable until the league's first match kicks off.
- **League/tournament winner prediction**: users pick the team they think will win. One pick per league, changeable until the league's first match kicks off.
- Predictions are editable until the match kicks off.
- Predictions are hidden from other users until kickoff.

### Scoring (configurable per league by admin)
- **Exact score**: e.g., 3 points (predicted 2-1 and result is 2-1).
- **Correct outcome**: e.g., 1 point (predicted a home win and it was a home win, but score was wrong).
- **Wrong prediction**: 0 points.
- **Top goalscorer bonus**: e.g., 10 points if the user's pick finishes as the top scorer.
- **Tournament/league winner bonus**: e.g., 10 points if the user's pick wins.

### Leaderboard & Standings
- Per-league leaderboard showing each user's total points, rank, and recent form.
- Group standings table (computed from fixture results in manual mode, or synced from API-Football).
- User's "My Predictions" view: list of all fixtures with their prediction, the result, and points earned.

### Admin Backoffice
- Manage users: view registered users, deactivate accounts.
- Create/edit leagues: set mode (manual/API), join code, configure point system.
- **Manual league management**:
  - Add/edit/remove teams (name + country code).
  - Organize teams into groups.
  - Create/edit fixtures (select teams, set kickoff, assign round).
  - Enter match results (set scores, mark as FINISHED — triggers scoring).
- Dashboard: overview of active leagues and user counts.

### Data Sync (API-Football) — API_SYNCED mode only
- A scheduled backend job (Spring `@Scheduled`) polls API-Football for:
  - Fixture list & kickoff times (daily or when admin creates a league).
  - Match results & scores (every 5 minutes during match days, less frequently otherwise).
  - Squad rosters (on league creation, refreshed weekly).
  - League standings (after each match day).
- Sync results are stored locally in PostgreSQL to avoid redundant API calls.

---

## Data Model

```
User
  id            UUID PK
  email         TEXT UNIQUE
  display_name  TEXT
  picture_url   TEXT
  role          TEXT (USER | ADMIN)
  is_active     BOOLEAN
  created_at    TIMESTAMPTZ

League
  id            UUID PK
  name          TEXT
  mode          TEXT (MANUAL | API_SYNCED)
  api_league_id INT NULL         -- only for API_SYNCED mode
  season        INT
  join_code     TEXT UNIQUE
  exact_score_points   INT DEFAULT 3
  correct_outcome_points INT DEFAULT 1
  wrong_prediction_points INT DEFAULT 0
  top_scorer_bonus     INT DEFAULT 10
  league_winner_bonus  INT DEFAULT 10
  created_at    TIMESTAMPTZ

LeagueMember
  id            UUID PK
  league_id     UUID FK -> League
  user_id       UUID FK -> User
  joined_at     TIMESTAMPTZ
  UNIQUE(league_id, user_id)

Team
  id            UUID PK
  league_id     UUID FK -> League
  name          TEXT
  country_code  TEXT             -- ISO 3166-1 alpha-2 (e.g., "br", "de")
  logo_url      TEXT NULL        -- derived from flagcdn.com or from API-Football
  group_name    TEXT NULL        -- e.g., "A", "B", ... "L"
  api_team_id   INT NULL         -- only for API_SYNCED mode
  UNIQUE(league_id, name)

Fixture
  id            UUID PK
  league_id     UUID FK -> League
  home_team_id  UUID FK -> Team
  away_team_id  UUID FK -> Team
  api_fixture_id INT NULL        -- only for API_SYNCED mode
  kickoff       TIMESTAMPTZ
  home_score    INT NULL         -- NULL until match finishes
  away_score    INT NULL
  status        TEXT             -- SCHEDULED | LIVE | FINISHED | POSTPONED | CANCELLED
  round         TEXT             -- e.g., "Group A", "Round of 16", "Quarter-final"
  matchday      INT              -- for ordering
  updated_at    TIMESTAMPTZ

Prediction
  id            UUID PK
  user_id       UUID FK -> User
  fixture_id    UUID FK -> Fixture
  home_score    INT
  away_score    INT
  points_earned INT NULL         -- calculated after match finishes
  created_at    TIMESTAMPTZ
  updated_at    TIMESTAMPTZ
  UNIQUE(user_id, fixture_id)

TopScorerPick
  id            UUID PK
  user_id       UUID FK -> User
  league_id     UUID FK -> League
  player_name   TEXT
  api_player_id INT NULL         -- NULL for manual leagues
  points_earned INT NULL
  UNIQUE(user_id, league_id)

LeagueWinnerPick
  id            UUID PK
  user_id       UUID FK -> User
  league_id     UUID FK -> League
  team_name     TEXT
  api_team_id   INT NULL         -- NULL for manual leagues
  points_earned INT NULL
  UNIQUE(user_id, league_id)

Player (optional — cached from API-Football for API_SYNCED leagues)
  id            UUID PK
  api_player_id INT
  api_team_id   INT
  league_id     UUID FK -> League
  name          TEXT
  photo_url     TEXT
  position      TEXT

Standing (group standings — computed from results in MANUAL mode, cached from API-Football in API_SYNCED mode)
  id            UUID PK
  league_id     UUID FK -> League
  team_id       UUID FK -> Team
  api_team_id   INT NULL
  team_name     TEXT
  team_logo     TEXT NULL
  group_name    TEXT NULL
  rank          INT
  points        INT
  played        INT
  won           INT
  drawn         INT
  lost          INT
  goals_for     INT
  goals_against INT
  goal_diff     INT
  updated_at    TIMESTAMPTZ

Note: The prediction leaderboard is computed at query time by summing
points_earned across Prediction, TopScorerPick, and LeagueWinnerPick
per user per league. No separate leaderboard table is needed.
```

---

## Key API Endpoints

| Method | Path                                  | Description                     |
|--------|---------------------------------------|---------------------------------|
| GET    | /api/auth/login                       | Initiate Google OAuth           |
| GET    | /api/auth/callback                    | OAuth callback                  |
| GET    | /api/users/me                         | Current user profile            |
| GET    | /api/leagues                          | List user's leagues             |
| POST   | /api/leagues/join                     | Join league by code             |
| GET    | /api/leagues/{id}                     | League detail                   |
| GET    | /api/leagues/{id}/fixtures            | Fixtures for a league           |
| GET    | /api/leagues/{id}/standings           | Group standings                 |
| GET    | /api/leagues/{id}/leaderboard         | Prediction leaderboard          |
| GET    | /api/leagues/{id}/predictions/me      | Current user's predictions      |
| PUT    | /api/predictions/{fixtureId}          | Create/update match prediction  |
| PUT    | /api/leagues/{id}/top-scorer-pick     | Set top scorer pick             |
| PUT    | /api/leagues/{id}/league-winner-pick  | Set league winner pick          |
| GET    | /api/admin/users                      | List all users (admin)          |
| PATCH  | /api/admin/users/{id}                 | Activate/deactivate user        |
| POST   | /api/admin/leagues                    | Create league (admin)           |
| PUT    | /api/admin/leagues/{id}               | Edit league settings (admin)    |
| GET    | /api/admin/leagues/{id}/teams         | List teams in league (admin)    |
| POST   | /api/admin/leagues/{id}/teams         | Add team to league (admin)      |
| PUT    | /api/admin/teams/{id}                 | Edit team (admin)               |
| DELETE | /api/admin/teams/{id}                 | Remove team (admin)             |
| POST   | /api/admin/leagues/{id}/fixtures      | Create fixture (admin)          |
| PUT    | /api/admin/fixtures/{id}              | Edit fixture / enter result     |
| DELETE | /api/admin/fixtures/{id}              | Remove fixture (admin)          |

---

## Pages

1. **Login** — Google sign-in button.
2. **Dashboard** — List of joined leagues with quick stats.
3. **League View** — Tabs: Fixtures & Predictions | Leaderboard | Standings.
4. **My Predictions** — All predictions with results and points.
5. **Join League** — Enter join code.
6. **Admin: Users** — Table of users with activate/deactivate toggle.
7. **Admin: Leagues** — Create league (manual or API-synced), configure points, view join code.
8. **Admin: Teams** — Add/edit teams, assign to groups (manual leagues).
9. **Admin: Fixtures** — Create/edit fixtures, enter match results (manual leagues).

---

## Milestones

### Milestone 1 — Auth & Project Skeleton ✅
- Set up monorepo: `/frontend` (Vite + React + TS + Tailwind + shadcn/ui) and `/backend` (Spring Boot + Kotlin + JPA + PostgreSQL).
- Google OAuth login flow (Spring Security OAuth2 + frontend redirect).
- User entity, auto-create on first login, admin role via `ADMIN_EMAIL` env var.
- Protected routes on frontend, auth context with TanStack Query.
- Basic layout shell (navbar, sidebar for admin).

### Milestone 2 — Leagues & Join Flow ✅
- Admin can create a league (manual mode or API-Football search).
- Users can join a league via join code.
- League view with fixture list grouped by matchday/round.
- Football standings table.

### Milestone 3 — Predictions & Scoring ✅
- Users can predict match scores (editable until kickoff, hidden from others).
- Top goalscorer and league winner picks.
- Scoring engine: calculates points when match results are entered (manual) or synced (API).
- "My Predictions" tab with results and points.

### Milestone 4 — Manual League Management (Teams, Fixtures, Results)
- Team entity with CRUD: name, country code, group assignment. Logos from flagcdn.com.
- Admin UI to add/edit/remove teams and organize into groups.
- Admin UI to create/edit fixtures (pick two teams, set kickoff, assign round).
- Admin UI to enter match results (set scores, mark FINISHED — triggers scoring).
- Group standings computed from fixture results.
- Refactor existing Fixture entity to reference Team entities instead of plain strings.

### Milestone 5 — Leaderboard & Admin Backoffice
- Leaderboard: ranked list with total points, correct scores, correct outcomes.
- Admin user management: view users, toggle active status.
- Admin league management: edit point system, view member counts.
- Responsive mobile design polish across all pages.

### Milestone 6 — Polish & Deploy
- Loading states, error handling, empty states across all pages.
- Mobile UX refinements (bottom nav, swipeable matchdays).
- OpenAPI spec generation from Spring Boot controllers.
- Dockerfiles for frontend and backend.
- AWS deployment: RDS, ECS/Fargate, S3 + CloudFront, CI/CD pipeline.

### Milestone 7 — API-Football Sync (Optional)
- API-synced league mode: admin selects league/season from API-Football.
- Scheduled sync for fixtures, results, squads, and standings.
- Auto-populate teams and fixtures from API data.
- This milestone is optional and only needed if using paid API-Football plans.
