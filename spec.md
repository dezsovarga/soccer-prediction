# Soccer Prediction App — Spec

## Overview

A web app where a group of friends predict soccer match scores, top goalscorers, and league winners. Users log in with Google, join leagues, and compete on a shared leaderboard. An admin manages users, leagues, and scoring rules. Match data is synced from API-Football.

---

## Tech Stack

| Layer       | Technology                                      |
|-------------|--------------------------------------------------|
| Frontend    | React 18, TypeScript, Vite, Tailwind CSS, shadcn/ui, TanStack Query, React Router |
| Backend     | Kotlin, Spring Boot 3, Spring Security (OAuth2), Spring Data JPA |
| Database    | PostgreSQL                                       |
| External    | API-Football (via RapidAPI) for fixtures, results, squads |
| Auth        | Google OAuth 2.0 (OIDC)                          |
| Hosting     | AWS (ECS/Fargate for backend, S3 + CloudFront for frontend, RDS for PostgreSQL) |

---

## Core Features

### Auth & Users
- Google OAuth login (OIDC). First login creates the user record.
- Admin role is granted to the user whose email matches the `ADMIN_EMAIL` env variable.
- Users have a display name and profile picture (pulled from Google).

### Leagues
- Admin creates a league by selecting a football league/season from API-Football.
- Each league has a unique join code. Users enter the code to join.
- Users can join multiple leagues.
- One shared leaderboard per league — all members compete together.

### Predictions
- **Match score predictions**: users predict the exact score (e.g., 2-1) for each fixture.
- **Top goalscorer prediction**: users pick one player from the API-Football squad roster (dropdown). One pick per league, changeable until the league's first match kicks off.
- **League winner prediction**: users pick the team they think will win the league. One pick per league, changeable until the league's first match kicks off.
- Predictions are editable until the match kicks off.
- Predictions are hidden from other users until kickoff.

### Scoring (configurable per league by admin)
- **Exact score**: e.g., 3 points (predicted 2-1 and result is 2-1).
- **Correct outcome**: e.g., 1 point (predicted a home win and it was a home win, but score was wrong).
- **Wrong prediction**: 0 points.
- **Top goalscorer bonus**: e.g., 10 points if the user's pick finishes as the league's top scorer.
- **League winner bonus**: e.g., 10 points if the user's pick wins the league.

### Leaderboard & Standings
- Per-league leaderboard showing each user's total points, rank, and recent form.
- Football league table (synced from API-Football) shown alongside predictions.
- User's "My Predictions" view: list of all fixtures with their prediction, the result, and points earned.

### Admin Backoffice
- Manage users: view registered users, deactivate accounts.
- Create/edit leagues: pick football league from API-Football, set join code, configure point system.
- Dashboard: overview of active leagues and user counts.

### Data Sync (API-Football)
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
  api_league_id INT          -- API-Football league ID
  season        INT          -- e.g., 2026
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

Fixture
  id            UUID PK
  league_id     UUID FK -> League
  api_fixture_id INT         -- API-Football fixture ID
  home_team     TEXT
  away_team     TEXT
  home_team_logo TEXT
  away_team_logo TEXT
  kickoff       TIMESTAMPTZ
  home_score    INT NULL     -- NULL until match finishes
  away_score    INT NULL
  status        TEXT         -- SCHEDULED | LIVE | FINISHED | POSTPONED
  matchday      INT
  updated_at    TIMESTAMPTZ

Prediction
  id            UUID PK
  user_id       UUID FK -> User
  fixture_id    UUID FK -> Fixture
  home_score    INT
  away_score    INT
  points_earned INT NULL     -- calculated after match finishes
  created_at    TIMESTAMPTZ
  updated_at    TIMESTAMPTZ
  UNIQUE(user_id, fixture_id)

TopScorerPick
  id            UUID PK
  user_id       UUID FK -> User
  league_id     UUID FK -> League
  player_name   TEXT
  api_player_id INT
  points_earned INT NULL
  UNIQUE(user_id, league_id)

LeagueWinnerPick
  id            UUID PK
  user_id       UUID FK -> User
  league_id     UUID FK -> League
  team_name     TEXT
  api_team_id   INT
  points_earned INT NULL
  UNIQUE(user_id, league_id)

Player (cached from API-Football)
  id            UUID PK
  api_player_id INT
  api_team_id   INT
  league_id     UUID FK -> League
  name          TEXT
  photo_url     TEXT
  position      TEXT

Note: The prediction leaderboard is computed at query time by summing
points_earned across Prediction, TopScorerPick, and LeagueWinnerPick
per user per league. No separate leaderboard table is needed.

Standing (cached from API-Football)
  id            UUID PK
  league_id     UUID FK -> League
  api_team_id   INT
  team_name     TEXT
  team_logo     TEXT
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
| GET    | /api/leagues/{id}                     | League detail + standings       |
| GET    | /api/leagues/{id}/fixtures            | Fixtures for a league           |
| GET    | /api/leagues/{id}/leaderboard         | Prediction leaderboard          |
| GET    | /api/leagues/{id}/predictions/me      | Current user's predictions      |
| PUT    | /api/predictions/{fixtureId}          | Create/update match prediction  |
| PUT    | /api/leagues/{id}/top-scorer-pick     | Set top scorer pick             |
| PUT    | /api/leagues/{id}/league-winner-pick  | Set league winner pick          |
| GET    | /api/admin/users                      | List all users (admin)          |
| PATCH  | /api/admin/users/{id}                 | Activate/deactivate user        |
| POST   | /api/admin/leagues                    | Create league (admin)           |
| PUT    | /api/admin/leagues/{id}               | Edit league settings (admin)    |

---

## Pages

1. **Login** — Google sign-in button.
2. **Dashboard** — List of joined leagues with quick stats.
3. **League View** — Tabs: Fixtures & Predictions | Leaderboard | Standings.
4. **Fixture Day** — List of matches for a matchday with prediction inputs.
5. **My Predictions** — All predictions with results and points.
6. **Join League** — Enter join code.
7. **Admin: Users** — Table of users with activate/deactivate toggle.
8. **Admin: Leagues** — Create league, configure points, view join code.

---

## Milestones

### Milestone 1 — Auth & Project Skeleton
- Set up monorepo: `/frontend` (Vite + React + TS + Tailwind + shadcn/ui) and `/backend` (Spring Boot + Kotlin + JPA + PostgreSQL).
- Google OAuth login flow (Spring Security OAuth2 + frontend redirect).
- User entity, auto-create on first login, admin role via `ADMIN_EMAIL` env var.
- Protected routes on frontend, auth context with TanStack Query.
- Basic layout shell (navbar, sidebar for admin).

### Milestone 2 — Leagues, Fixtures & API-Football Sync
- Admin can create a league (search/select from API-Football leagues).
- Scheduled job syncs fixtures, squads, and standings from API-Football.
- Users can join a league via join code.
- League view with fixture list grouped by matchday.
- Football standings table pulled from cached data.

### Milestone 3 — Predictions & Scoring
- Users can predict match scores (editable until kickoff, hidden from others).
- Top goalscorer and league winner picks (dropdown from API-Football data).
- Scoring engine: calculates points when match results are synced.
- "My Predictions" page with results and points.

### Milestone 4 — Leaderboard & Admin Backoffice
- Leaderboard: ranked list with total points, correct scores, correct outcomes.
- Admin user management: view users, toggle active status.
- Admin league management: edit point system, view member counts.
- Responsive mobile design polish across all pages.

### Milestone 5 — Polish & Deploy
- Loading states, error handling, empty states across all pages.
- Mobile UX refinements (bottom nav, swipeable matchdays).
- OpenAPI spec generation from Spring Boot controllers.
- Dockerfiles for frontend and backend.
- AWS deployment: RDS, ECS/Fargate, S3 + CloudFront, CI/CD pipeline.
