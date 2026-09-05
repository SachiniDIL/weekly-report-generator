CREATE TYPE review_action AS ENUM ('APPROVED', 'CHANGES_REQUESTED');

CREATE TABLE review_comments (
    id                 BIGSERIAL PRIMARY KEY,
    report_version_id BIGINT        NOT NULL REFERENCES report_versions (id),
    manager_id         BIGINT        NOT NULL REFERENCES users (id),
    action             review_action NOT NULL,
    -- Required for CHANGES_REQUESTED, optional for APPROVED — enforced at the service layer,
    -- not here, so this stays nullable.
    comment            TEXT,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_review_comments_report_version_id ON review_comments (report_version_id);
