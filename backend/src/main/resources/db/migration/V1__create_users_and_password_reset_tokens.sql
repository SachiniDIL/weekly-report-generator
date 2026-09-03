CREATE TYPE user_role AS ENUM ('ADMIN', 'MANAGER', 'MEMBER');
CREATE TYPE user_status AS ENUM ('PENDING', 'ACTIVE');

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          user_role,
    status        user_status  NOT NULL DEFAULT 'PENDING',
    token_version INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE password_reset_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);
CREATE INDEX idx_password_reset_tokens_token_hash ON password_reset_tokens (token_hash);
