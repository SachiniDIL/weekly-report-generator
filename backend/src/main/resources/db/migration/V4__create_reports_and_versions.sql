CREATE TYPE report_status AS ENUM ('DRAFT', 'SUBMITTED', 'NEEDS_CORRECTION', 'APPROVED');

CREATE TABLE reports (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT        NOT NULL REFERENCES users (id),
    project_id         BIGINT        NOT NULL REFERENCES projects (id),
    week_start         DATE          NOT NULL,
    week_end           DATE          NOT NULL,
    status             report_status NOT NULL DEFAULT 'DRAFT',
    current_version_no INTEGER       NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE report_versions (
    id                 BIGSERIAL PRIMARY KEY,
    report_id          BIGINT      NOT NULL REFERENCES reports (id),
    version_no         INTEGER     NOT NULL,
    submitted_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    tasks_planned_next TEXT,
    notes              TEXT,
    links              TEXT
);

CREATE TABLE task_entries (
    id                 BIGSERIAL PRIMARY KEY,
    report_version_id BIGINT       NOT NULL REFERENCES report_versions (id),
    task_name          VARCHAR(255) NOT NULL,
    priority           VARCHAR(50)  NOT NULL,
    planned_pct        INTEGER      NOT NULL,
    actual_pct         INTEGER      NOT NULL,
    status             VARCHAR(50)  NOT NULL,
    time_planned       INTEGER,
    time_spent         INTEGER,
    deliverable        TEXT
);

CREATE TABLE blockers (
    id                 BIGSERIAL PRIMARY KEY,
    report_version_id BIGINT  NOT NULL REFERENCES report_versions (id),
    description        TEXT    NOT NULL,
    is_key_issue       BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE achievements (
    id                 BIGSERIAL PRIMARY KEY,
    report_version_id BIGINT  NOT NULL REFERENCES report_versions (id),
    description        TEXT    NOT NULL,
    is_key_highlight   BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE hours_breakdowns (
    id                 BIGSERIAL PRIMARY KEY,
    report_version_id BIGINT       NOT NULL REFERENCES report_versions (id),
    task_type          VARCHAR(100) NOT NULL,
    hours              NUMERIC      NOT NULL
);

CREATE INDEX idx_reports_user_id ON reports (user_id);
CREATE INDEX idx_reports_project_id ON reports (project_id);

-- Also an FK, per the same index-every-FK precedent as the *_report_version_id columns below —
-- version history lookups (find all versions of a report) will need this.
CREATE INDEX idx_report_versions_report_id ON report_versions (report_id);

CREATE INDEX idx_task_entries_report_version_id ON task_entries (report_version_id);
CREATE INDEX idx_blockers_report_version_id ON blockers (report_version_id);
CREATE INDEX idx_achievements_report_version_id ON achievements (report_version_id);
CREATE INDEX idx_hours_breakdowns_report_version_id ON hours_breakdowns (report_version_id);

-- Database-enforced "flag exactly one as key" rule: a partial unique index rejects a second
-- true row per report_version_id outright, rather than relying on application code to remember.
CREATE UNIQUE INDEX uq_blockers_one_key_issue_per_version
    ON blockers (report_version_id)
    WHERE is_key_issue = true;

CREATE UNIQUE INDEX uq_achievements_one_key_highlight_per_version
    ON achievements (report_version_id)
    WHERE is_key_highlight = true;
