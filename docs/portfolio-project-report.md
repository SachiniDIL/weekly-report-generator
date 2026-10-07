# Weekly Report Generator — Portfolio Project Report

> Purpose of this document: a complete, factual brief of the project, written so it can be handed to an AI assistant (or a person) with the request "add this project to my portfolio". Everything below is taken from the actual codebase. Items marked **[fill in]** are things only the owner can supply.

---

## 1. At a glance

|                        |                                                                                                                                                                                                |
| ---------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Name**               | Weekly Report Generator                                                                                                                                                                        |
| **One-line pitch**     | A team weekly-reporting platform: members write structured weekly reports, managers review and approve them (or send them back with comments), and an AI assistant summarises the team's week. |
| **Type**               | Full-stack web application (solo build)                                                                                                                                                        |
| **Role**               | Sole developer — design, backend, frontend, database, tests, AI integration                                                                                                                    |
| **Status**             | Feature-complete and fully tested locally. Deployment configuration (Dockerfile for the backend) is prepared; **[fill in: live URL if deployed]**                                              |
| **Repo**               | **[fill in: GitHub URL]**                                                                                                                                                                      |
| **Live demo**          | **[fill in]**                                                                                                                                                                                  |
| **Screenshots to add** | Manager dashboard (charts + AI summary), report editor, review page with version history, "New report" coverage picker, section comparison                                                     |

### Short description (for a project card, ~40 words)

A full-stack weekly team-reporting tool. Members file structured weekly reports; managers approve them or request changes, with every revision kept as immutable history. A manager dashboard visualises team progress, and a Gemini-powered assistant answers questions about the reports.

### Medium description (for a project page, ~120 words)

Weekly Report Generator replaces ad-hoc status emails with a structured, auditable workflow. Team members write a weekly report per project — tasks with planned vs. actual progress, blockers, achievements and an hours breakdown — and submit it for review. Managers approve it or send it back with a comment, which opens a new editable version while preserving the full history of what was submitted and what the manager said. Managers get a dashboard with compliance metrics and four charts, a side-by-side comparison of every member's blockers or achievements, per-member profiles, and an AI chat assistant (Google Gemini) that answers questions grounded only in the reports in scope. Admins control who gets in: sign-ups are approval-gated and role-assigned.

---

## 2. The problem and the users

Teams that report progress by email or chat lose history, can't compare people or weeks, and have no clear "who hasn't submitted" signal. This app gives three kinds of users three purpose-built experiences:

| Role        | What they do                                                                                                                                                                                                                       |
| ----------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Member**  | Writes a weekly report per assigned project, saves drafts, submits for review, fixes reports sent back, can see the full version history of their own reports.                                                                     |
| **Manager** | Reviews submitted reports (approve / request changes with a comment), watches a team dashboard, compares sections across members, opens member profiles, manages projects and project membership, asks the AI assistant questions. |
| **Admin**   | Approves new sign-ups and assigns roles, creates and removes users, protected from locking the system out (can't remove or demote the last admin).                                                                                 |

---

## 3. Key features

### Reporting & review workflow

- **Structured weekly report** per (member, project, week): task entries (name, priority, planned %, actual %, status, time planned/spent, deliverable), blockers (with one "key issue" marker), achievements (with one "key highlight"), an hours breakdown by task type (Development / Testing / Meetings / Documentation), plus notes, links and "planned for next week".
- **Draft → Submitted → Approved / Needs correction** state machine.
- **Versioned corrections**: when a manager requests changes, the system forks a new version N+1, deep-copies all content forward, and freezes version N together with the manager's comment. History is never mutated — a complete audit trail of what was submitted and what was said.
- **Version history view** for both sides: members see their own past versions and the feedback on each; managers see "Previous versions" on the review page.
- **Read-only report view** once a report is submitted or approved.
- **Review queue** for managers (submitted reports, oldest week first).

### Guided "new report" flow (recently added)

- A **"Where you stand" coverage table**: for every project a member is assigned to, whether they've started / submitted a report for this week and last week, with a one-click start.
- **Week picker limited to the current week and the previous five** — it is impossible to pick an upcoming week (also enforced on the server with a 422).
- **Duplicate protection**: projects already reported for the chosen week are disabled in the picker and flagged "already started"; the server also rejects a second report for the same member + project + week (409). This was built in response to a real usability problem — users filling in a whole form only to be rejected at save time.

### Manager dashboard

- Four summary cards: submitted this week, **compliance rate**, needs-correction count, open blockers.
- Four Recharts charts: tasks-completed trend, submission status by member, workload by project, time by task type.
- **Section comparison**: pick Blockers or Achievements and a week, and see every active member's items side by side (current version only), including members who submitted nothing.
- **Per-member profile**: totals, needs-correction count, hours by task type, and that member's full report history.

### AI assistant (Google Gemini, manager-only)

- A floating chat widget and a one-click "Generate AI summary" panel.
- The backend builds a plain-text **digest of the reports in scope** (using the same role-scoping as the rest of the app) and prompts the model to answer **only from that context** and to say so when the answer isn't there — a deliberate grounding/hallucination control.
- The weekly summary uses a fixed structure: _Completed work / Recurring blockers / Workload imbalances_.
- All Gemini failures (timeout, non-2xx, empty reply) collapse into one friendly "temporarily unavailable" message; details are logged server-side only.
- Replies are rendered through a small custom, dependency-free Markdown renderer.

### Auth & administration

- **Approval-gated registration**: sign-up creates a `PENDING`, role-less account; an admin approves it and assigns a role.
- JWT authentication (HS256, 24 h) with a **`tokenVersion` claim** that lets the server revoke every outstanding token for a user without a blacklist (used on password reset and user removal).
- **Password reset by email** (Gmail SMTP). The raw token is emailed; only its SHA-256 hash is stored; single-use; the endpoint always returns the same generic response so it can't be used to discover which emails exist.
- **Rate limiting** (Bucket4j, per-IP) on login (10/min), register (5/min) and forgot-password (5/min).
- Soft-deleted ("removed") users, so reports keep their foreign-key integrity.

### UX details

- Role-specific sidebar, collapsing to a slide-in drawer on mobile; attention markers (a dot on "Reports" when a correction is requested, a count on "Review Queue").
- Toast notifications, a promise-based confirm dialog for hard-to-undo actions, skeleton loaders, deterministic gradient avatars, backend error messages surfaced to the user.

---

## 4. Tech stack

| Layer               | Technology                                                                                                        |
| ------------------- | ----------------------------------------------------------------------------------------------------------------- |
| **Frontend**        | Next.js 15 (App Router) · React 19 · TypeScript · TanStack Query v5 · Tailwind CSS v4 · Recharts · lucide-react   |
| **Backend**         | Java 21 · Spring Boot 4.1 (Spring 7) · Spring Security 7 · Spring Data JPA / Hibernate · Bean Validation · Lombok |
| **Database**        | PostgreSQL 16 · Flyway migrations (V1–V5) · Hibernate in `validate` mode                                          |
| **Auth / security** | JWT (jjwt) · BCrypt · Bucket4j rate limiting · method security (`@PreAuthorize`) · locked-down CORS               |
| **AI**              | Google Gemini via the Generative Language REST API (`RestClient`, called from the backend only)                   |
| **Email**           | Spring `JavaMailSender` over Gmail SMTP                                                                           |
| **Testing**         | JUnit 5 + MockMvc + **Testcontainers (real PostgreSQL)** · Jest 30 + React Testing Library                        |
| **Tooling / infra** | Docker Compose (local DB) · multi-stage Dockerfile for the backend · ESLint · Prettier · Maven wrapper            |

---

## 5. Architecture

```
Browser ──► Next.js 15 (App Router, React 19)
              │  TanStack Query hooks → lib/api-client.ts (bearer token, typed ApiError)
              ▼
         Spring Boot 4 REST API (stateless)
              CorsFilter → RateLimitFilter → JwtAuthenticationFilter → @PreAuthorize
              Controllers (thin) → Services (business logic, @Transactional) → Repositories (JPA)
              │                                      │
              ▼                                      ▼
         PostgreSQL 16 (Flyway-owned schema)   Gemini API  /  Gmail SMTP
```

**Backend.** Conventional layered design. Controllers only bind the request and delegate. Services own transactions and are split narrowly (read vs. write, self-service vs. admin). DTOs are Java records used only at the HTTP boundary — entities never leave the service layer. A single `@RestControllerAdvice` turns typed exceptions into a uniform `{ message, fieldErrors? }` body so no stack trace ever leaks. Dynamic report filtering uses JPA `Specification`s.

**Frontend.** Route groups `(auth)`, `(member)`, `(manager)`, `(admin)`, each wrapped in a role guard that waits for session rehydration, redirects anonymous users to login and wrong-role users to their own landing page. All server state goes through TanStack Query hooks; the API client is unaware of how the session is stored.

**Access control — three layers.**

1. _Authentication_: the JWT filter validates the token, reloads the user and checks `tokenVersion`.
2. _Coarse authorisation_: `@PreAuthorize` role gates on controllers/services.
3. _Row-level authorisation_ inside services: a member can only edit/submit their own report; a member's report list is silently scoped to themselves and an explicit request for someone else's reports is _rejected_ (403) rather than overridden; admins are blocked from report data entirely.

---

## 6. Data model

Eleven tables across five Flyway migrations.

- `users` (role, status `PENDING/ACTIVE/REMOVED`, `token_version`), `password_reset_tokens` (hashed, single-use, expiring)
- `projects` (archived rather than deleted), `project_assignments` (composite-key join table that drives "which projects can this member report on")
- `reports` — one row per (member, project, week) with status and `current_version_no`
- `report_versions` — one row per revision
- `task_entries`, `blockers`, `achievements`, `hours_breakdowns` — all keyed to the **version**, not the report, which is what makes history immutable
- `review_comments` — each manager verdict is recorded against the specific version it was made on

Notable database decisions:

- **Partial unique indexes** (`WHERE is_key_issue = true`) enforce "at most one key blocker / key highlight per version" in Postgres itself, not just in application code.
- Native PostgreSQL enum types; an explicit index on every foreign key; `ddl-auto=validate` so the schema is owned solely by migrations.

---

## 7. Engineering highlights & problems solved

These make good talking points for interviews and the portfolio write-up.

1. **Immutable audit trail with editable drafts.** Content is replaced in place _within_ a version, but "request changes" always forks a new version and deep-copies forward, so no historical row is ever modified.
2. **Hibernate flush-ordering bug.** Re-saving a report that had a key blocker hit a unique-index violation because Hibernate orders INSERTs before DELETEs in one flush. Fixed with an explicit `flush()` between delete and re-insert, plus a regression test.
3. **Stateless JWT revocation.** Added a `tokenVersion` claim compared against the DB so a password reset or user removal instantly logs out all of that user's sessions, with no server-side blacklist.
4. **No account enumeration.** Password-reset and login flows return identical responses whether or not an email exists; the email provider was swapped from a REST API to SMTP without changing that contract.
5. **LLM latency and grounding.** A reasoning model took 20–40 s and blew a hard-coded timeout; fixed by making the timeout configurable and lowering the model's thinking level. Prompts constrain the model to the supplied context so it doesn't invent facts.
6. **Spring Boot 4 / Jackson 3 migration.** `ObjectMapper` moved packages; security filters were updated accordingly.
7. **Filter ordering.** Hand-built filters are constructed inside `SecurityConfig` rather than being `@Component`s, so Spring Boot doesn't also register them ahead of CORS.
8. **Test isolation vs. rate limiting.** A global limiter made shared-context auth tests trip each other; disabled in the test profile and re-enabled in one dedicated, isolated test.
9. **UX-driven redesign of the create flow.** A user-facing flaw (wasted effort followed by a 409) led to a coverage table, a constrained week picker, disabled-if-taken project options, and matching server-side guards — validation both prevents the mistake in the UI _and_ is enforced in the API.
10. **Dashboard correctness.** Fixed the compliance-rate calculation so it measures the right population; section comparison always uses a report's _current_ version only.
11. **Realistic demo data through real code paths.** The seeder builds ~48 reports across six weeks, in every status, including reports taken through two correction cycles, by calling the real services — so the demo data is exactly what the endpoints would produce.

---

## 8. Testing & quality

- **Backend:** 139 passing tests (JUnit 5, MockMvc, Spring Security Test) running against a **real PostgreSQL container via Testcontainers** — not an in-memory substitute. Covers auth, role gating, row-level access, the review/correction workflow, version history, dashboard aggregations, rate limiting, AI endpoint access (Gemini mocked), duplicate/future-week guards.
- **Frontend:** 142 passing tests (Jest + React Testing Library) covering pages, forms, query hooks, role guards, charts and the AI widgets.
- TypeScript strict type-checking and ESLint clean; Prettier-formatted.

---

## 9. Demo / how to run

- Local stack: `docker compose up -d` (Postgres 16) → `./mvnw spring-boot:run -Dspring-boot.run.profiles=local,seed` (API on :8080) → `npm run dev` (UI on :3000).
- The `seed` profile loads 3 managers, 9 members, 5 projects and ~48 reports so the dashboard is populated.
- Seeded accounts (demo only): `manager1@seed.dev` … `manager3@seed.dev`, `member1@seed.dev` … `member9@seed.dev`, all with password `Seeded@123`.
- A recruiter-friendly demo path: log in as a member → _New report_ (see the coverage table) → save and submit; log in as a manager → _Review Queue_ → request changes; switch back to the member → fix and resubmit; manager → _Dashboard_ → _Generate AI summary_.

---

## 10. Scale at a glance

- ~129 backend source files (controllers, services, repositories, DTOs, domain, typed exceptions) and 17 routed pages.
- 34 REST endpoints across 9 controllers; 3 roles; 5 migrations.
- ~280 automated tests in total.

---

## 11. Honest limitations / future work

(Good to include — it shows judgement.)

- Not yet deployed publicly; the backend Dockerfile is ready for a host such as Render, with the frontend suited to Vercel.
- Single 24 h JWT, no refresh token; token held in `sessionStorage` (an httpOnly cookie would reduce XSS exposure).
- AI replies are one blocking request (no streaming) and the chat has no multi-turn memory.
- Only the password-reset email is sent — no notifications on submit / changes-requested yet.
- Task priority/status values are constrained in the UI but not yet validated server-side.
- Possible next steps: date-range reports, CSV/PDF export, Redis-backed rate limiting, structured logging and metrics, end-to-end tests.

---

## 12. Suggested portfolio entry

**Title:** Weekly Report Generator — structured team reporting with AI summaries

**Tagline:** Full-stack Spring Boot + Next.js app with a versioned review workflow, role-based access, a manager analytics dashboard and a Gemini-powered assistant.

**Bullets (resume/portfolio style):**

- Designed and built a full-stack weekly reporting platform (Spring Boot 4 / Java 21, Next.js 15 / React 19, PostgreSQL) with three roles and approval-gated sign-up.
- Implemented an immutable, versioned review workflow: requesting changes forks a new version and preserves every earlier submission and manager comment as audit history.
- Enforced access control at three layers (JWT filter, `@PreAuthorize`, row-level service checks) and added JWT revocation via a `tokenVersion` claim, per-IP rate limiting and enumeration-safe password reset.
- Built a manager dashboard with compliance metrics, four Recharts visualisations, a cross-member section comparison and per-member profiles.
- Integrated Google Gemini to answer questions and produce weekly summaries, grounding the model on a server-built digest of the reports the manager is allowed to see; handled latency and failure modes gracefully.
- Improved UX from real feedback: a guided create-report flow (coverage table, bounded week picker, disabled duplicates) backed by matching server-side validation.
- Wrote ~280 automated tests, including integration tests against a real PostgreSQL via Testcontainers.

**Tags:** Java · Spring Boot · Spring Security · JWT · PostgreSQL · Flyway · Testcontainers · Next.js · React · TypeScript · TanStack Query · Tailwind CSS · Recharts · Google Gemini · REST API design · Role-based access control

---

_Note for whoever consumes this brief: the credentials listed in section 9 are demo seed data for a local environment only. No real API keys, passwords or secrets appear in this document and none should be added to a public portfolio page._
