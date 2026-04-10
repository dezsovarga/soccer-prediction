# CLAUDE.md

## Project Overview

Web application with a React frontend and Kotlin Spring Boot backend, using PostgreSQL for persistence and Google OAuth for authentication.

## Monorepo Structure

```
/frontend   — React + TypeScript + Tailwind CSS + shadcn/ui
/backend    — Kotlin + Spring Boot REST API + PostgreSQL
```

- **API contract**: `/backend/openapi.yaml` is the single source of truth for the API spec.
- Backend uses feature-based package structure (e.g., `com.example.match`, `com.example.user`).
- Frontend uses path aliases: `@/components`, `@/hooks`, `@/lib`, etc.

## Local Development Setup

### Prerequisites

- **Node 22** — managed via nvm (`nvm use` reads `.nvmrc`)
- **Java 17+** — required by Spring Boot / Maven
- **Docker** — PostgreSQL runs in a Docker container (see `docker-compose.yml`)
- **Google OAuth credentials** — create at [Google Cloud Console](https://console.cloud.google.com/apis/credentials), set authorized redirect URI to `http://localhost:8080/api/auth/callback`

### Database

PostgreSQL runs via Docker Compose, not locally. The `dev.sh` script starts it automatically. To start it manually:

```bash
docker compose up -d --wait
```

This creates a `soccer_prediction` database on `localhost:5432` (user: `postgres`, password: `postgres`).

To connect via psql inside the container:

```bash
docker exec -it soccer-prediction-db psql -U postgres -d soccer_prediction
```

### Environment Variables

Set these before running the backend (e.g., via shell export or IDE run config):

```
GOOGLE_CLIENT_ID=<your-google-client-id>
GOOGLE_CLIENT_SECRET=<your-google-client-secret>
ADMIN_EMAIL=<your-google-email>
```

Optional (defaults shown):

```
DB_USERNAME=postgres
DB_PASSWORD=postgres
FRONTEND_URL=http://localhost:5173
```

### Starting the Backend

```bash
cd backend
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`. JPA auto-creates tables on first run (`ddl-auto: update`).

### Starting the Frontend

```bash
cd frontend
nvm use
npm install
npm run dev
```

Runs on `http://localhost:5173`. Talks to backend at `http://localhost:8080` (configurable via `VITE_API_URL`).

### Running Tests

```bash
# Backend (19 tests — uses H2 in-memory DB, no Postgres needed)
cd backend && ./mvnw test

# Frontend (18 tests)
cd frontend && npm test -- --run
```

## Code Style

### Frontend

- Functional components only — no class components.
- Never use `any`; always provide explicit types.
- Use `interface` (not `type`) for object shapes.
- Use TanStack Query for all server state / data fetching.

### Backend

- Use `data class` for DTOs.
- Prefer `val` over `var`.
- Use `sealed class` / `sealed interface` for result types.
- Never expose JPA entity classes directly in API responses — always map to DTOs.

### Shared Conventions

- All dates must be in UTC.
- Error responses must follow a structured format (e.g., `{ error: string, code: string, details?: object }`).

## Testing

### Rules

- Always write unit tests for every new feature before considering it done.
- Always write integration tests for every REST API endpoint.
- Tests live next to the code they test, not in a separate top-level folder.
- Never skip tests with `@Disabled` or `.skip` without a comment explaining why.
- A feature is not complete until its tests pass.

### Frameworks

- **Frontend**: Vitest + React Testing Library.
- **Backend**: JUnit 5 + MockK for unit tests, Spring Boot Test for integration tests.

### Milestone Implementation Order

1. Write the backend endpoint + unit tests.
2. Write the integration test for the endpoint.
3. Write the frontend component + its tests.
4. Only mark the milestone done when all tests pass.

## Rules

- **Never commit secrets or API keys.** Use environment variables or a secrets manager.
- **API changes must be end-to-end.** When modifying a backend endpoint, always update:
  1. The OpenAPI spec (`/backend/openapi.yaml`)
  2. The frontend consumer (TanStack Query hooks, types, etc.)
- **Keep the OpenAPI spec in sync** with the actual Spring Boot controller implementations at all times.
