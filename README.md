# weekly-report-generator

## Project overview

A weekly team-reporting tool. Team members write a structured weekly report per
project — task entries with planned/actual progress, blockers, achievements, and
an hours breakdown — and submit it for review. Managers approve reports or send
them back with comments, which opens a new editable version while keeping the
full history. A manager dashboard aggregates the data into summary metrics and
charts (submission status, tasks completed, workload by project, time by task
type), a side-by-side section comparison across the team, and per-member
profiles. An AI chat assistant (Google Gemini Flash) answers questions and
generates a team summary from the reports currently in scope.

- **Backend** — Spring Boot 4 (Java 21), PostgreSQL, Flyway migrations, JWT auth.
  Lives in `backend/`.
- **Frontend** — Next.js 15 (App Router, React 19), TanStack Query, Tailwind.
  Lives in `frontend/`.

## Prerequisites

| Tool                    | Version         | Used for                      |
| ----------------------- | --------------- | ----------------------------- |
| Node.js                 | 20 LTS or newer | frontend dev server and build |
| Java (JDK)              | 21              | backend build and run         |
| Docker + Docker Compose | recent          | local PostgreSQL database     |

The backend uses the Maven wrapper (`./mvnw`), so a separate Maven install is not
required.

## Installing dependencies

```bash
# frontend
cd frontend
npm install

# backend (from the backend/ directory)
cd backend
./mvnw install        # use mvnw.cmd on Windows
```

## Environment configuration

### Backend

Local runs use `backend/src/main/resources/application-local.properties`, which is
**git-ignored** because it holds real secrets (JWT signing key, Gmail app password,
Gemini API key, datasource credentials). Create it from the values described in
`.env.example`. The base `application.properties` reads everything else from
environment variables and is what production uses.

Password-reset email is sent over Gmail SMTP. `application-local.properties` needs:

```properties
GMAIL_ADDRESS=your.email@gmail.com
GMAIL_APP_PASSWORD=your 16-character app password
```

`GMAIL_APP_PASSWORD` is a Google **app password** (Account → Security → App
passwords), not the account password. `spring.mail.host` / `port` and the SMTP
auth/STARTTLS flags are already set in the base `application.properties`. If the
credentials are missing or wrong the send fails silently and the
`/auth/forgot-password` response is unchanged, by design.

`server.port` is `${PORT:8080}` — it binds to the platform-assigned `$PORT` in
production and falls back to `8080` locally.

### Frontend

Copy `frontend/.env.local.example` to `frontend/.env.local`:

```bash
cd frontend
cp .env.local.example .env.local
```

`NEXT_PUBLIC_API_URL` is the only variable — the base URL of the backend API
(`http://localhost:8080` for local dev). The `NEXT_PUBLIC_` prefix is required
because the API client runs in client components.

## Running the database

Local development uses a PostgreSQL container defined in `docker-compose.yml`.

```bash
docker compose up -d
```

This starts the `db` service on `localhost:5432` with database, user, and
password all set to `weekly_report`, matching the defaults in `.env.example`.
Data persists in the `weekly-report-db-data` volume across restarts.

```bash
docker compose down      # stop the database
docker compose down -v   # stop and delete all data
```

## Running the backend

With the database running, from `backend/`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The `local` profile activates `application-local.properties` and a
`LocalAdminSeeder` that creates one admin account (`admin@local.dev` /
`localadmin123`). Flyway applies the schema on startup. The API is served on
`http://localhost:8080`.

## Running the frontend

From `frontend/`:

```bash
npm run dev
```

The app runs on `http://localhost:3000` and talks to the backend at
`NEXT_PUBLIC_API_URL`.

## Seed data

For a populated dashboard (multiple members, several weeks of reports across all
statuses, version history, varied hours), activate the `seed` profile alongside
`local` on a **first** run:

```bash
# from backend/
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,seed
```

`ProductionSeeder` is idempotent — it checks for a seeded project before doing
anything, so leaving the profile on for later runs is harmless, but it is only
needed once. To seed the deployed instance, add `seed` to
`SPRING_PROFILES_ACTIVE` for one boot, then remove it.

### Seeded demo credentials

Password for **every** seeded account: `Seeded@123`

| Role    | Email               |
| ------- | ------------------- |
| Manager | `manager1@seed.dev` |
| Manager | `manager2@seed.dev` |
| Member  | `member1@seed.dev`  |
| Member  | `member2@seed.dev`  |
| Member  | `member3@seed.dev`  |
| Member  | `member4@seed.dev`  |
| Member  | `member5@seed.dev`  |

The `local` profile additionally seeds an admin: `admin@local.dev` /
`localadmin123`.

## Testing

```bash
cd frontend && npm test        # Jest + Testing Library
cd backend  && ./mvnw test     # JUnit + Testcontainers (needs Docker running)
```

## Deployment

The backend deploys to **Render** (Docker) and the frontend to **Vercel**. Code
is deploy-ready; configuration is done in each dashboard.

### Backend on Render

Deploy `backend/` using its `Dockerfile`. Render injects `$PORT` at runtime and
the app already binds to it. Set these environment variables:

| Variable                                                    | Notes                                                                                 |
| ----------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| `SPRING_PROFILES_ACTIVE`                                    | leave unset normally; set to `seed` for one boot to load demo data, then remove       |
| `SPRING_DATASOURCE_URL`                                     | Render PostgreSQL connection string                                                   |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | database credentials                                                                  |
| `JWT_SECRET`                                                | random string, at least 32 characters                                                 |
| `FRONTEND_URL`                                              | **the production Vercel URL** (e.g. `https://your-app.vercel.app`)                    |
| `GMAIL_ADDRESS` / `GMAIL_APP_PASSWORD`                      | Gmail SMTP for password-reset email; app password, not the account password           |
| `GEMINI_API_KEY` / `GEMINI_MODEL`                           | AI assistant; use a current model id such as `gemini-3.6-flash` (AI Studio free tier) |
| `RATE_LIMIT_ENABLED`                                        | optional; per-IP limiting on the `/auth` endpoints, on unless set to `false`          |

**CORS:** the backend allows exactly one origin, taken from `FRONTEND_URL` — the
same variable used to build password-reset links. Setting `FRONTEND_URL` to the
real Vercel domain is all that is needed; there is no separate CORS setting.
Until it is set to the deployed frontend's URL, browser requests from the
deployed frontend will be blocked by CORS.

### Frontend on Vercel

Import `frontend/` as the project root. In **Project Settings → Environment
Variables**, set:

| Variable              | Value                                                                  |
| --------------------- | ---------------------------------------------------------------------- |
| `NEXT_PUBLIC_API_URL` | the deployed Render backend URL (e.g. `https://your-api.onrender.com`) |

The build reads `NEXT_PUBLIC_API_URL` at build time, so a redeploy is required
after changing it. There is no `.env.local` in production — this value must come
from the Vercel dashboard.
