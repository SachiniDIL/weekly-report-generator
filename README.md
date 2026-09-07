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

## How it runs locally

Three processes, started in this order:

| #   | Process  | How                                   | URL / port              |
| --- | -------- | ------------------------------------- | ----------------------- |
| 1   | Database | `docker compose up -d` (repo root)    | `localhost:5432`        |
| 2   | Backend  | `./mvnw spring-boot:run` (`backend/`) | `http://localhost:8080` |
| 3   | Frontend | `npm run dev` (`frontend/`)           | `http://localhost:3000` |

The frontend talks to the backend, the backend talks to the database. Open
`http://localhost:3000` in a browser once all three are up.

## Prerequisites

Install these first. Nothing else is needed — the backend ships a Maven wrapper
(`./mvnw`), so you do **not** need a separate Maven install.

| Tool                    | Version         | Used for                      |
| ----------------------- | --------------- | ----------------------------- |
| Node.js                 | 20 LTS or newer | frontend dev server and build |
| Java (JDK)              | 21              | backend build and run         |
| Docker + Docker Compose | recent          | local PostgreSQL database     |

Check they are on your PATH:

```bash
node --version     # v20.x or newer
java --version     # 21.x
docker --version   # any recent
```

---

## Setup

Run these steps once. Later you only need the three "start" commands in
[How it runs locally](#how-it-runs-locally).

### 1. Install dependencies

```bash
# frontend
cd frontend
npm install
cd ..

# backend — downloads Maven + all Java dependencies (slow the first time)
cd backend
./mvnw install -DskipTests    # on Windows: mvnw.cmd install -DskipTests
cd ..
```

### 2. Configure the backend

The backend reads secrets and machine-specific settings from
`backend/src/main/resources/application-local.properties`. This file is
**git-ignored** and does not exist yet — create it with this content:

```properties
# --- database (matches docker-compose.yml) ---
spring.datasource.url=jdbc:postgresql://localhost:5432/weekly_report
spring.datasource.username=weekly_report
spring.datasource.password=weekly_report

# --- auth: any string of at least 32 characters ---
jwt.secret=local-dev-secret-change-me-but-keep-at-least-32-characters-long

# --- public URL of the frontend, used in email links and CORS ---
frontend.url=http://localhost:3000

# --- AI assistant (Google AI Studio, free tier) ---
# A real key from https://aistudio.google.com/apikey turns the assistant on.
# A dummy value is fine to start the app — the assistant then just reports
# "temporarily unavailable".
gemini.api-key=your_gemini_api_key
gemini.model=gemini-3.6-flash

# --- password-reset email over Gmail SMTP ---
# GMAIL_APP_PASSWORD is a 16-character Google *app password*
# (Google account > Security > 2-Step Verification > App passwords),
# NOT your normal account password. Dummy values are fine to start the app —
# the reset email then fails silently and the endpoint behaves the same.
GMAIL_ADDRESS=your.email@gmail.com
GMAIL_APP_PASSWORD=your app password
```

Everything else (SMTP host/port, STARTTLS, timeouts) is already set in the
committed `application.properties`.

### 3. Configure the frontend

```bash
cd frontend
cp .env.local.example .env.local     # Windows PowerShell: copy .env.local.example .env.local
cd ..
```

`NEXT_PUBLIC_API_URL` is the only variable; the default `http://localhost:8080`
is correct for local development.

---

## Running the project

### 1. Start the database

From the repository root:

```bash
docker compose up -d
```

This starts a `postgres:16` container on `localhost:5432` with database, user,
and password all `weekly_report`. Data persists in the `weekly-report-db-data`
volume across restarts.

```bash
docker compose ps        # check it is healthy
docker compose down      # stop it (data kept)
docker compose down -v   # stop it and delete all data
```

### 2. Start the backend

From `backend/`, with the database running:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows: `mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"`

The `local` profile activates `application-local.properties`. On startup Flyway
creates the schema, and a `LocalAdminSeeder` creates one admin account:

| Role  | Email             | Password        |
| ----- | ----------------- | --------------- |
| Admin | `admin@local.dev` | `localadmin123` |

The API is served on `http://localhost:8080`. Leave this terminal running.

### 3. Start the frontend

In a second terminal, from `frontend/`:

```bash
npm run dev
```

Open `http://localhost:3000`. Sign in with the admin account above, or register
a new account from the sign-up page.

---

## Seed data (optional)

The admin-only account gives you an empty app. For a populated dashboard —
3 managers, 9 members, 5 projects and ~48 reports spread over six weeks across
every status, with version history and varied hours — add the `seed` profile
**on a first run**:

```bash
# from backend/
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,seed
```

`ProductionSeeder` is idempotent — it checks for a seeded project before doing
anything, so it is safe to leave the profile on, but it only needs to run once.

### Seeded accounts

Password for **every** seeded account: `Seeded@123`

| Role    | Emails                                    |
| ------- | ----------------------------------------- |
| Manager | `manager1@seed.dev` … `manager3@seed.dev` |
| Member  | `member1@seed.dev` … `member9@seed.dev`   |

The `local` profile still also seeds the admin (`admin@local.dev` /
`localadmin123`).

---

## Running the tests

```bash
cd frontend && npm test        # Jest + Testing Library
cd backend  && ./mvnw test     # JUnit + Testcontainers — needs Docker running
```

The backend tests start their own throwaway PostgreSQL container via
Testcontainers, so Docker must be available, but you do **not** need
`docker compose up` for them.

---

## Troubleshooting

| Symptom                                                 | Fix                                                                                                                     |
| ------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| Backend fails to start, `Could not resolve placeholder` | A key is missing from `application-local.properties` — compare against the block in [step 2](#2-configure-the-backend). |
| Backend fails to start, connection refused on 5432      | The database container is not up. Run `docker compose up -d` and `docker compose ps`.                                   |
| Backend starts but `Flyway ... validate` fails          | Schema drift from an earlier run. Reset the database: `docker compose down -v && docker compose up -d`.                 |
| Frontend loads but every request fails / CORS error     | Backend not running, or `frontend.url` in `application-local.properties` is not `http://localhost:3000`.                |
| AI assistant says "temporarily unavailable"             | Expected without a real `gemini.api-key`. Add one from Google AI Studio to enable it.                                   |
| Password-reset email never arrives                      | Expected without real `GMAIL_ADDRESS` / `GMAIL_APP_PASSWORD`. The endpoint still returns normally.                      |
| Port 8080 or 3000 already in use                        | Stop the other process, or change the port (`server.port` / `next dev -p <port>` plus `NEXT_PUBLIC_API_URL`).           |
