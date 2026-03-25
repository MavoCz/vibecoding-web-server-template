# Spring Boot + React Template

A full-stack modular monolith template with a **Spring Boot 4** backend and **React** SPA frontend. Includes JWT authentication, SSE notifications, and a module system ready for extension.

| Layer | Stack |
|-------|-------|
| Backend | Java 25, Spring Boot 4, Spring Modulith, JOOQ, Flyway, PostgreSQL |
| Frontend | React 19, TypeScript, Vite, MUI v6, Zustand, TanStack Query v5 |
| Testing | JUnit 5, Playwright (API + browser E2E) |

## Mac Development Setup

### 1. Install Homebrew

If you don't have Homebrew yet:

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

Follow the post-install instructions to add Homebrew to your PATH.

### 2. Install Git

```bash
brew install git
```

Verify:

```bash
git --version
```

Configure your identity:

```bash
git config --global user.name "Your Name"
git config --global user.email "your@email.com"
```

### 3. Install Docker

Install [Docker Desktop for Mac](https://www.docker.com/products/docker-desktop/):

```bash
brew install --cask docker
```

Open Docker Desktop once to complete setup, then verify:

```bash
docker --version
```

Docker must be running whenever you start the app or run JOOQ code generation.

### 4. Install Java 25 via SDKMAN

```bash
curl -s "https://get.sdkman.io" | bash
```

Open a new terminal (or run `source "$HOME/.sdkman/bin/sdkman-init.sh"`), then:

```bash
sdk install java 25.0.2-tem
```

The project includes an `.sdkmanrc` file, so SDKMAN will auto-switch to the correct Java version when you `cd` into the project (if you have `sdkman_auto_env=true` in `~/.sdkman/etc/config`).

Verify:

```bash
java -version
# Expected: openjdk version "25.0.2" ...
```

### 5. Install Node.js and pnpm

```bash
brew install node
brew install pnpm
```

Verify:

```bash
node --version   # 20+ required
pnpm --version   # 9+ required
```

### 6. Clone and install dependencies

```bash
git clone <repository-url>
cd template
pnpm install
```

### 7. Install Playwright browsers (for E2E tests)

```bash
pnpm --filter e2e exec playwright install chromium
```

## Running the App

### Quick start (backend + frontend together)

```bash
./dev.sh
```

This starts both the backend (port 8080) and the Vite dev server (port 5173). PostgreSQL is auto-started via Docker Compose — no manual database setup needed.

- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html

Press `Ctrl+C` to stop everything.

### Start services individually

```bash
# Backend (auto-starts PostgreSQL via Docker Compose)
cd backend && ./gradlew bootRun

# Frontend dev server (separate terminal, from project root)
pnpm dev
```

### Fresh database

To reset the database and re-run migrations from scratch:

```bash
docker compose -f backend/compose.yml down -v
# Then restart the app
```

## Development Commands

### Frontend (from project root)

| Command | Description |
|---------|-------------|
| `pnpm dev` | Start Vite dev server with API proxy to backend |
| `pnpm build` | Production build into `backend/src/main/resources/static/` |
| `pnpm generate-api` | Export OpenAPI spec + regenerate TypeScript types & hooks |
| `pnpm test:api` | Run API-only E2E tests (no browser needed) |
| `pnpm test:web` | Run full-stack browser E2E tests |

### Backend (from `backend/`)

| Command | Description |
|---------|-------------|
| `./gradlew bootRun` | Run the app (port 8080, auto-starts PostgreSQL) |
| `./gradlew build` | Full build with tests |
| `./gradlew test` | Run all tests |
| `./gradlew test --tests "ClassName"` | Run a single test class |
| `./gradlew jooqCodegen` | Regenerate JOOQ classes from DB schema (requires Docker) |
| `./gradlew generateOpenApiDocs` | Export OpenAPI spec to `build/docs/openapi.json` |
| `./gradlew bootJar` | Build executable JAR |

## Project Structure

```
template/
├── backend/           # Spring Boot 4 backend
│   ├── src/main/java/net/voldrich/myhome/backend/
│   │   ├── auth/              # Auth module (JWT, login, register)
│   │   ├── notification/      # Notification module (SSE, persistence)
│   │   └── web/               # SPA forwarding controller
│   ├── src/main/resources/
│   │   ├── db/migration/      # Flyway SQL migrations
│   │   └── application.properties
│   ├── buildSrc/              # JOOQ codegen with Testcontainers
│   ├── compose.yml            # PostgreSQL for development
│   └── build.gradle.kts
├── common/            # Shared TypeScript API types + fetch client
├── web/               # React SPA (Vite, MUI, React Router, Zustand)
├── e2e/               # Playwright E2E tests (API + UI)
├── orval.config.ts    # API codegen configuration
├── dev.sh             # Start backend + frontend together
└── package.json       # pnpm workspace root
```

## Architecture

### Backend — Spring Modulith

Modules are sub-packages under `net.voldrich.myhome.backend`. Each module exposes a public API at its root; anything in `internal/` is hidden from other modules.

**Auth module** — Simple email/password authentication with JWT access tokens (15 min) and rotating refresh tokens (7 days). Exposes `AuthModuleApi` for other modules to get the current user.

**Notification module** — SSE-based real-time notifications with persistence. Exposes `NotificationModuleApi` for other modules to query notification state.

### Frontend — React SPA

- **pnpm workspaces** — `common/` (shared API types) and `web/` (React SPA)
- **Orval** generates TypeScript types and React Query hooks from the backend's OpenAPI spec
- **Zustand** manages auth state with localStorage persistence
- **Module system** — register new modules in `web/src/modules/registry.ts` for automatic dashboard tiles and navigation

### Database

- **PostgreSQL 18** via Docker Compose (auto-managed by Spring Boot)
- **JOOQ** for type-safe SQL queries (not an ORM)
- **Flyway** for schema migrations (`V{n}__{description}.sql`)

## Adding a New Module

1. Write a Flyway migration in `backend/src/main/resources/db/migration/`
2. Run `./gradlew jooqCodegen` to regenerate JOOQ table classes
3. Create the backend module under `net.voldrich.myhome.backend.<name>/` with controllers, services, repositories
4. Run `pnpm generate-api` to regenerate TypeScript types
5. Add frontend pages in `web/src/modules/<name>/`
6. Register the module in `web/src/App.tsx` and add routes in `web/src/router.tsx`
7. Write E2E tests in `e2e/tests/`

## API Code Generation

The project uses [Orval](https://orval.dev) to generate TypeScript types and API hooks from the backend's OpenAPI spec:

```bash
pnpm generate-api
```

This starts the backend, exports the OpenAPI spec, stops the backend, and runs Orval. Output goes to:
- `common/src/api/generated/` — TypeScript types + fetch functions
- `web/src/api/generated/` — React Query hooks

Generated files are committed so frontend builds work without the backend running. Re-run after any backend endpoint change.

## E2E Tests

Playwright tests live in `e2e/` with two projects:

- **`api`** — HTTP-only tests against the backend (no browser)
- **`ui`** — Full-stack browser tests (Chromium) against the React app

Tests auto-start a dedicated Postgres (port 5435) and backend (port 8081) — no manual setup needed.

```bash
pnpm test:api                # API tests
pnpm test:web                # Browser tests

# Single file
cd e2e && pnpm exec playwright test tests/api/auth.spec.ts --project=api
```

## Production Build

```bash
pnpm build                   # Build SPA into backend static resources
cd backend && ./gradlew bootJar  # Build executable JAR
java -jar backend/build/libs/backend-*.jar  # Run (configure DB via env vars)
```

The SPA is served at the root URL. All client-side routes are forwarded to `index.html`.
