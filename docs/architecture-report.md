# Weekly Report Generator — Architecture & Design Report

A structured walkthrough of how the system is built, covering architecture, data
model, the key frontend surfaces, the API and its access control, the
review/correction workflow, the AI assistant, the notable problems solved along
the way, and where it could go next.

---

## 1. System architecture overview

The project is two independently deployable applications talking over a JSON REST
API, with a stateless authentication model.

| Layer    | Stack                                                                      | Location                                  |
| -------- | -------------------------------------------------------------------------- | ----------------------------------------- |
| Frontend | Next.js 15 (App Router), React 19, TanStack Query 5, Tailwind v4, Recharts | `frontend/`                               |
| Backend  | Spring Boot 4.1 / Spring 7 / Java 21, Spring Security 7, Spring Data JPA   | `backend/`                                |
| Database | PostgreSQL 16, schema owned by Flyway migrations                           | `backend/src/main/resources/db/migration` |
| AI       | Google Gemini (Generative Language REST API)                               | called from the backend only              |

### Backend shape

A conventional layered design:

- **Controllers** (`controller/`) are thin — they bind the request, pull the
  authenticated user with `@AuthenticationPrincipal User`, and delegate.
- **Services** (`service/`) hold all business logic and own transactions
  (`@Transactional`). Responsibilities are split narrowly: read vs write
  (`ReportQueryService` vs `ReportService`), self-service vs admin
  (`AuthService` vs `AdminUserService`), manager project lifecycle vs a member's
  own project list (`ProjectService` vs `AssignedProjectService`). Small shared
  helpers (`CurrentReportVersionFinder`, `DashboardCalendar`, `TeamRosterForWeek`)
  keep one definition of a concept in one place.
- **Repositories** are Spring Data JPA interfaces; dynamic report filtering uses a
  JPA `Specification` (`ReportSpecifications`).
- **DTOs** (`dto/`) are Java `record`s used only at the HTTP boundary. Domain
  entities never leave the service layer.
- **Exception handling** — typed exceptions extend `ApiException` (which carries
  an `HttpStatus`); a single `@RestControllerAdvice` (`GlobalExceptionHandler`)
  turns them, plus validation and access-denied errors, into a uniform
  `{ "message": ..., "fieldErrors": {...}? }` body. Nothing else leaks a stack
  trace or exception type.

### Security / request pipeline

`SecurityConfig` runs the app **stateless** (`SessionCreationPolicy.STATELESS`),
CSRF disabled (no cookies), CORS locked to a single origin taken from
`frontend.url`. The filter order is:

```
CorsFilter → RateLimitFilter → JwtAuthenticationFilter → Spring authorization
```

- `RateLimitFilter` (Bucket4j) — per-IP token buckets on the three public
  `/auth` endpoints only (login 10/min, register 5/min, forgot-password 5/min);
  429 + `Retry-After: 60` on exhaustion.
- `JwtAuthenticationFilter` — validates the `Authorization: Bearer` token,
  re-loads the `User` from the DB, checks the token's `tokenVersion` still
  matches the stored one, and sets the authenticated principal to the `User`
  entity with a `ROLE_<X>` authority.
- Method security (`@EnableMethodSecurity` + `@PreAuthorize`) does the
  role gating.

Both hand-built filters are `new`-ed inside `SecurityConfig` rather than being
`@Component`s, specifically so Spring Boot does not _also_ register them as
top-level servlet filters that would run ahead of CORS.

### Frontend shape

- **Route groups** — `(auth)`, `(member)`, `(manager)`, `(admin)`. Each
  protected group's `layout.tsx` wraps its subtree in `RoleGuardedSection`, which
  uses `useRequireAuth` → the pure, unit-testable `evaluateAuthGuard`: it waits
  out session rehydration, redirects anonymous users to `/login`, and redirects
  wrong-role users to their own landing route (`/reports`, `/projects`,
  `/admin/users`).
- **AppShell** — one shared frame: a role-specific sidebar (nav items from
  `nav-config`), a topbar with a route-derived page title and a time-of-day
  greeting, and a user footer (avatar, role badge, sign out).
- **Server state** — every backend call goes through `lib/api-client.ts`
  (`request()`): injects the bearer token, serialises query params the way Spring
  `Pageable` expects (repeated `sort` keys), and throws a typed `ApiError`
  carrying the backend's own `message`. All fetching is wrapped in TanStack Query
  hooks (`lib/**/use-*-query.ts`, `use-*-mutation.ts`).
- **Session** — `AuthProvider` keeps `{token, user}` in `sessionStorage`,
  rehydrates on mount, exposes `login`/`logout`. The API client is told the token
  via `setAuthToken` so it stays unaware of how the session is persisted.

### Bootstrapping

- `LocalAdminSeeder` (profile `local`) — one admin account for local dev.
- `ProductionSeeder` (profile `seed`) — an idempotent demo dataset (3 managers,
  9 members, 5 projects, ~48 reports over six weeks in every status, three
  reports carried through two correction cycles). It builds the data **through
  the real services**, so version history, review comments and status
  transitions match exactly what the endpoints produce.

---

## 2. Database design explanation

Five Flyway migrations (`V1`–`V5`). Hibernate is set to `ddl-auto=validate` — it
only maps to the schema, never changes it. Enums are native PostgreSQL enum
types. Every foreign key column has an explicit index (a stated convention in the
migration comments).

### Tables

**`users`** — `id`, `name`, `email` (unique), `password_hash` (BCrypt), `role`
(`ADMIN` / `MANAGER` / `MEMBER`, _nullable_ — unset until an admin approves),
`status` (`PENDING` / `ACTIVE` / `REMOVED`), `token_version` (int), `created_at`.

- `token_version` is the JWT-revocation lever: bump it and every token already
  issued to that user stops validating. Used on password reset **and** on user
  removal.
- `REMOVED` is a soft delete (added in `V2`) so that reports and assignments can
  keep referencing the row by FK. A removed account behaves exactly like one that
  never existed.

**`password_reset_tokens`** — `user_id`, `token_hash` (SHA-256 of the raw token —
the raw value is emailed and never stored), `expires_at`, `used_at` (single use),
`created_at`. Indexed on `user_id` and `token_hash`.

**`projects`** — `id`, `name`, `description`, `is_active`. "Deleting" a project
archives it (`is_active = false`) because reports reference it by FK.

**`project_assignments`** — pure join table, composite PK `(user_id, project_id)`,
index on `project_id`. Drives both "who is on this project" and "which projects
can this member report on".

**`reports`** — `user_id`, `project_id`, `week_start`, `week_end`, `status`
(`DRAFT` / `SUBMITTED` / `NEEDS_CORRECTION` / `APPROVED`), `current_version_no`,
timestamps. One row per (member, project, week); it points at its current
version.

**`report_versions`** — `report_id`, `version_no`, `submitted_at`, and the
free-text fields `tasks_planned_next`, `notes`, `links`. **One row per
revision.** A correction creates a new version rather than editing the old one.

**Version content tables** — all foreign-keyed to `report_versions` (not to
`reports`), which is what makes history immutable:

| Table              | Columns                                                                                                     |
| ------------------ | ----------------------------------------------------------------------------------------------------------- |
| `task_entries`     | `task_name`, `priority`, `planned_pct`, `actual_pct`, `status`, `time_planned`, `time_spent`, `deliverable` |
| `blockers`         | `description`, `is_key_issue`                                                                               |
| `achievements`     | `description`, `is_key_highlight`                                                                           |
| `hours_breakdowns` | `task_type`, `hours` (NUMERIC)                                                                              |

Two **partial unique indexes** enforce "at most one key item per version" in the
database itself rather than trusting application code:

```sql
CREATE UNIQUE INDEX uq_blockers_one_key_issue_per_version
    ON blockers (report_version_id) WHERE is_key_issue = true;
CREATE UNIQUE INDEX uq_achievements_one_key_highlight_per_version
    ON achievements (report_version_id) WHERE is_key_highlight = true;
```

**`review_comments`** — `report_version_id`, `manager_id`, `action`
(`APPROVED` / `CHANGES_REQUESTED`), `comment` (nullable in the schema; required
for `CHANGES_REQUESTED` at the service layer), `created_at`. Each manager verdict
is recorded against the specific version it was made on.

### Why it is shaped this way

- **Versioning by copy-forward** — the audit trail (what the report looked like
  at each review, and what the manager said) is preserved without ever mutating a
  historical row.
- **Content keyed to the version, not the report** — an old version's tasks and
  blockers stay exactly as they were submitted.
- **Constraints in the database** — uniqueness of a key blocker/highlight, FK
  integrity, and enum validity are all enforced by Postgres.

---

## 3. Key frontend components

### Member "My Reports" (personal page) — `app/(member)/reports/page.tsx`

The member's home (their post-login landing route). A paginated list of **only
their own reports** (`useMyReportsQuery` → `GET /reports`, which the backend
auto-scopes to the caller), each row a status badge + project + week linking to
the editor, plus a "Create new report" button.

Supporting surfaces:

- **Report editor** (`report-editor-form.tsx`, `use-report-content-form.ts`) —
  used for both create (`/reports/new`) and edit (`/reports/[id]`). An identity
  block (project picker fed by `GET /me/projects`, week start/end) plus repeated
  fieldsets for tasks / blockers / achievements / hours, with client-side
  validation. When the report is `NEEDS_CORRECTION`, the manager's
  `CorrectionNotice` is rendered above the form.
- **Read-only view** (`ReportContentView`) — shown instead of the editor once a
  report is `SUBMITTED` or `APPROVED`.

### Report version history — `lib/reports/version-history-list.tsx`

Backed by `GET /reports/{id}/versions` (`use-report-version-history-query`).
Returns newest-first; **each entry carries its own content snapshot and the
review comment made against that version**. It appears in two places: on the
member's editor (to see what a past version contained and why it came back), and
on the manager's review page as "Previous versions".

### Team dashboard — `app/(manager)/dashboard/page.tsx`

Manager-only, two tabs:

- **Overview** —
  - Four summary cards from `GET /dashboard/summary`: submitted this week,
    compliance rate, needs-correction count, open blockers.
  - Four Recharts charts from `GET /dashboard/charts/*`: tasks-completed trend,
    submission status by member, workload by project, time by task type.
  - A "Generate team summary" button + dismissable AI panel (`team-summary-panel`).
- **Section comparison** (`section-comparison.tsx`) — pick _Blockers_ or
  _Achievements_ and a week; `GET /dashboard/section` returns, side by side,
  every active member's items for that week's **current version only**, with the
  key item visually distinguished and members who submitted nothing still listed
  with their status chip.

### Related manager surfaces

- **Review queue** (`/review`) — `GET /reports?status=SUBMITTED`, oldest week
  first; each row links to the review page.
- **Team roster** (`/team` → `/team/[userId]`) — a member profile combining
  `GET /dashboard/team/{userId}` stats (non-draft report count, needs-correction
  count, hours by task type) with that member's report history
  (`GET /reports?userId=`).

Shared building blocks: `AppShell`, `Avatar` (deterministic gradient from the
name), `ReportStatusBadge`, `MemberWeekStatusChip`, skeleton loaders, and
`describeError` so the backend's own error text reaches the user.

---

## 4. API design and backend structure, including role-based access

REST, resource-oriented, JSON in/out. Controllers are grouped by resource; each
either carries a class-level `@PreAuthorize` or defers authorization to its
services.

### Endpoint map

| Controller                | Base path      | Access                                | Endpoints                                                                                                                                                                    |
| ------------------------- | -------------- | ------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `AuthController`          | `/auth`        | public                                | `POST /register` (201, creates a `PENDING` account), `POST /login` (returns a JWT)                                                                                           |
| `PasswordResetController` | `/auth`        | public                                | `POST /forgot-password`, `POST /reset-password` — always a generic response                                                                                                  |
| `AdminUserController`     | `/admin/users` | `ADMIN`                               | `GET` (list, optional status filter), `POST` (create active user), `POST /{id}/approve`, `DELETE /{id}`, `PATCH /{id}/role`                                                  |
| `UserController`          | `/users`       | `MANAGER` or `ADMIN`                  | `GET` — user directory                                                                                                                                                       |
| `ProjectController`       | `/projects`    | `MANAGER`                             | project CRUD + `GET/POST/DELETE /{id}/members`                                                                                                                               |
| `MyProjectController`     | `/me/projects` | any authenticated                     | `GET` — the caller's own active assigned projects                                                                                                                            |
| `ReportController`        | `/reports`     | authenticated (row-level in services) | `GET` (list), `GET /{id}`, `POST`, `PUT /{id}`, `POST /{id}/submit`, `POST /{id}/review` (MANAGER), `GET /{id}/versions`                                                     |
| `DashboardController`     | `/dashboard`   | `MANAGER`                             | `/summary`, `/section`, `/team/{userId}`, `/charts/tasks-completed-trend`, `/charts/submission-status-by-member`, `/charts/workload-by-project`, `/charts/time-by-task-type` |
| `AiController`            | `/ai`          | `MANAGER`                             | `POST /chat`, `GET /summary`                                                                                                                                                 |

### Role-based access — three layers

1. **Authentication** — `JwtAuthenticationFilter` verifies the HS256 token
   (24 h TTL, claims: `role`, `status`, `tokenVersion`), re-loads the user,
   rejects the token if `tokenVersion` no longer matches or the user has no role,
   and grants a single `ROLE_<X>` authority.
2. **Coarse authorization** — `@PreAuthorize("hasRole('MANAGER')")` etc. at the
   class or method level for whole-resource gates.
3. **Fine authorization** — inside the services, where role alone is not enough:
   - `ReportService.getOwnedReport` — a member can only edit/submit **their own**
     report.
   - `ReportQueryService.scopeToCaller` — a member's report list is silently
     narrowed to their own; an explicit request for someone else's `userId` is
     _rejected_, not overridden. A manager's filters (including an optional
     `userId`) are honoured. Admins are blocked from reports entirely.
   - `ReportQueryService.canView` — per-row visibility on report detail
     (`MANAGER` any, `MEMBER` own, `ADMIN` none).

`AccessDeniedException` from any layer becomes a `403` with body
`{"message":"Access denied"}`.

### Cross-cutting

- **Registration is approval-gated** — self-registration creates a `PENDING`,
  role-less account; an admin approves it and assigns the role. Login on a
  `PENDING` account returns a specific "pending approval" message.
- **Token revocation** — `tokenVersion` is bumped on password reset and on user
  removal, instantly invalidating every outstanding token for that user without a
  server-side blacklist.
- **Rate limiting** — Bucket4j per-IP on the three unauthenticated `/auth`
  endpoints (everything else already needs a valid JWT, which bounds abuse).

---

## 5. How the report review / correction workflow was implemented

The workflow is a state machine on `reports.status`:

```
DRAFT ──submit──▶ SUBMITTED ──approve──────▶ APPROVED   (terminal)
                     │
                     └──request changes──▶ NEEDS_CORRECTION ──submit──▶ SUBMITTED ──▶ …
```

### Authoring (`ReportService`)

- `POST /reports` creates the report and **version 1**, status `DRAFT`.
- `PUT /reports/{id}` replaces the _current version's_ content wholesale
  (`replaceContent`: delete all child rows for the version, `flush()`, re-insert).
  Editable only in `DRAFT` or `NEEDS_CORRECTION` (`EDITABLE_STATUSES`).
- `POST /reports/{id}/submit` (owner only) → status `SUBMITTED`, `submitted_at`
  stamped, the version is now frozen.

### Review (`ReportReviewService.review`, `POST /reports/{id}/review`, MANAGER)

Valid only while the report is `SUBMITTED`.

1. A `review_comments` row is written against the **reviewed version** (manager,
   action, comment).
2. **APPROVED** → status `APPROVED`. The reviewed version stays current and
   frozen. End of the line.
3. **CHANGES_REQUESTED** (comment required — enforced in the service) → status
   `NEEDS_CORRECTION`, and `openNewVersionFrom`:
   - creates version _N+1_,
   - deep-copies every task / blocker / achievement / hours row and the free-text
     fields from version _N_ (`copyContentForward`),
   - repoints `current_version_no` at _N+1_.

   Version _N_ and its review comment are now immutable history.

### Correction loop (member)

The member opens the report, sees `CorrectionNotice` — the manager's comment,
located by `findCorrectionComment` scanning the version history — above the
editor, revises version _N+1_, and resubmits. Status goes back to `SUBMITTED` and
the loop repeats until approval. The seed data includes reports taken through two
full cycles (ending at version 3).

### History

`GET /reports/{id}/versions` returns every version newest-first, each bundled
with its own content snapshot and the review comment made against it — the same
payload powers the member's "what changed" view and the manager's "Previous
versions" panel.

The guiding idea, stated in the service's own Javadoc: _approving freezes the
current version in place; requesting changes freezes it as commented-on history
and opens a new editable version copied forward from it._

---

## 6. AI Chat Assistant approach (Good to Have — implemented)

Manager-only. Two capabilities: free-form chat about the team's reports, and a
fixed-format weekly summary.

### Backend

- **`GeminiConfig`** — a dedicated `RestClient` bean for Google's Generative
  Language API (`/v1beta`), auth via the `x-goog-api-key` header, 5 s connect
  timeout, configurable read timeout (default 60 s). Model id is configurable
  (`gemini.model`, currently `gemini-3.6-flash`).
- **`GeminiService.generate(prompt)`** — one-shot `generateContent`. **No
  multi-turn memory, no persistence of chat history.** Every failure mode — a
  transport error, a non-2xx response, or an empty candidate — is turned into a
  single fixed string, _"The AI assistant is temporarily unavailable…"_; the
  caller never sees an exception or a stack trace. The HTTP status and body are
  logged server-side. The request sends `thinkingConfig.thinkingLevel: "LOW"` to
  keep latency down.
- **`ReportContextBuilder.buildDigest`** — pulls the reports matching the
  manager's active dashboard filter via `ReportQueryService` (**same role-scoping
  as the rest of the app**), and renders them as a **plain-text digest**, one
  section per member, prose rather than JSON — tasks with planned/actual/status,
  blockers (with a `KEY ISSUE` marker), achievements, hours. A sentinel string is
  returned when nothing matches.
- **`AiService`** (`@PreAuthorize("hasRole('MANAGER')")`):
  - `chat(question, filter)` — prompt instructs the model to answer _only_ from
    the context and to say so explicitly if the answer isn't there.
  - `summary(filter)` — fixed prompt: summarise under exactly _Completed work /
    Recurring blockers / Workload imbalances_.
- **`AiController`** — `POST /ai/chat` (question + optional filter),
  `GET /ai/summary`.

### Frontend

- A floating chat widget mounted in the `(manager)` layout
  (`ai-chat-widget` / `ai-chat-thread` / `ai-chat-composer`) — messages in React
  state only, input disabled while a request is in flight, the backend's error
  message shown inline.
- A "Generate team summary" button + dismissable panel on the dashboard Overview
  tab.

Tests mock `GeminiService` with `@MockitoBean`: a `MEMBER` gets 403, and a
`MANAGER` call is verified to reach Gemini with a non-empty prompt containing the
seeded report data.

---

## 7. Challenges faced and how they were solved

| #   | Challenge                                                                                                                                                                                                                                                       | Resolution                                                                                                                                                                                 |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1   | **Spring Boot 4 ships Jackson 3** — `ObjectMapper` moved to `tools.jackson.databind`, so filters that serialise error bodies wouldn't compile against the old `com.fasterxml.jackson.databind` import.                                                          | Import the new package in the security filters; annotations stay on `com.fasterxml`.                                                                                                       |
| 2   | **`PUT /reports/{id}` hit a duplicate-key error** on `uq_blockers_one_key_issue_per_version` when re-saving a key blocker/achievement — Hibernate orders INSERTs before DELETEs within one flush, so the new key row collided with the not-yet-deleted old one. | Explicit `flush()` between the deletes and the re-inserts in `replaceContent`, plus a regression test.                                                                                     |
| 3   | **Gemini read-timeout** — `gemini-3.6-flash` is a reasoning model; real prompts took 20–40 s and blew a hard-coded 20 s read timeout (`SocketTimeoutException`).                                                                                                | Made the timeout configurable (default 60 s) and sent `thinkingLevel: "LOW"` to cut latency to seconds; log HTTP status/body on failure.                                                   |
| 4   | **Rate limiting broke the test suite** — the auth integration tests share one Spring context (one bucket map), so a global limiter made them trip each other.                                                                                                   | `rate-limit.enabled=false` in the test profile; the dedicated `RateLimitIntegrationTest` re-enables it for its own isolated context with a fixed `X-Forwarded-For`.                        |
| 5   | **"Functions cannot be passed to Client Components"** — passing lucide icon components in a nav array from a Server layout to the client `AppShell`.                                                                                                            | `AppShell` takes a `variant` string and imports the nav arrays itself.                                                                                                                     |
| 6   | **Members got 403 on the create-report project dropdown** — `ProjectController` is entirely MANAGER-gated.                                                                                                                                                      | A separate `MyProjectController` / `AssignedProjectService` at `/me/projects` with no role gate, returning only the caller's own assigned active projects.                                 |
| 7   | **Filter registration order** — hand-built filters risked being auto-registered by Spring Boot ahead of the CORS filter.                                                                                                                                        | `new` them in `SecurityConfig` rather than making them `@Component`s.                                                                                                                      |
| 8   | **Stateless JWTs can't be individually revoked** — a leaked token stays valid until expiry; a password reset must log the old session out.                                                                                                                      | A `tokenVersion` claim compared against the stored column; bump it to invalidate every token for a user (used on reset and removal).                                                       |
| 9   | **Email provider swap** — started on the Brevo REST API, moved to Spring `JavaMailSender` over Gmail SMTP.                                                                                                                                                      | Kept the identical best-effort contract (log + swallow `MailException`) so `/auth/forgot-password` responds the same whether or not delivery succeeds — no account-existence leak.         |
| 10  | **Wholesale content replace vs an immutable audit trail**                                                                                                                                                                                                       | Reconciled by versioning: content is replaced in place _within_ a version, but "request changes" always forks a new version and deep-copies forward, so no historical row is ever mutated. |

---

## 8. Possible future improvements

**Deployment & ops**

- Actually deploy (Render for the backend Docker image, Vercel for the frontend —
  configuration is prepared but not executed).
- Structured logging, request tracing, and Micrometer metrics; explicit
  health/readiness endpoints.
- Move Bucket4j state (and any future caching) to Redis so limits survive
  restarts and work across multiple instances.

**Auth**

- Short-lived access tokens plus refresh tokens (currently a single 24 h token,
  no refresh).
- Consider an httpOnly cookie for the token to shrink the XSS token-theft surface
  (brings CSRF handling back into scope).
- An audit log of admin actions (role changes, removals); bulk approve.

**AI assistant**

- Stream responses (SSE) instead of a single 20–40 s blocked request.
- Let the chat widget pass the dashboard's active filter (it currently sends
  none) and add lightweight multi-turn context.
- Cache the context digest per filter.

**Reporting & dashboard**

- Validate `priority` / task `status` against enums instead of free-form
  `VARCHAR(50)` strings.
- Date-range selection rather than a single week; CSV/PDF export.
- In-app / email notifications when a report is submitted (manager) or sent back
  (member) — right now the only transactional email is password reset.
- Server-side pagination for the section-comparison view at larger team sizes.

**Frontend**

- Optimistic updates on mutations.
- Consolidate the remaining inline styles into shared design tokens.
- Accessibility and internationalisation passes.

**Testing**

- End-to-end tests against a running stack; a contract test pinning the Gemini
  response shape.
