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

## Rules

- **Never commit secrets or API keys.** Use environment variables or a secrets manager.
- **API changes must be end-to-end.** When modifying a backend endpoint, always update:
  1. The OpenAPI spec (`/backend/openapi.yaml`)
  2. The frontend consumer (TanStack Query hooks, types, etc.)
- **Keep the OpenAPI spec in sync** with the actual Spring Boot controller implementations at all times.
