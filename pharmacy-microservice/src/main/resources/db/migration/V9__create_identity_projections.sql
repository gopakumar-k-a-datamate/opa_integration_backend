-- ============================================================
-- V9: Identity Read Model Projections
-- Defines local read-model tables to cache users, roles, and mappings
-- ============================================================

CREATE TABLE IF NOT EXISTS projection_user (
    id          UUID PRIMARY KEY,
    username    VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projection_role (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projection_user_role (
    user_id     UUID NOT NULL,
    role_id     UUID NOT NULL,
    PRIMARY KEY (user_id, role_id)
);
