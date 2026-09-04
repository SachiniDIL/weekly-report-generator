CREATE TABLE projects (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT true
);

CREATE TABLE project_assignments (
    user_id    BIGINT NOT NULL REFERENCES users (id),
    project_id BIGINT NOT NULL REFERENCES projects (id),
    PRIMARY KEY (user_id, project_id)
);

CREATE INDEX idx_project_assignments_project_id ON project_assignments (project_id);
