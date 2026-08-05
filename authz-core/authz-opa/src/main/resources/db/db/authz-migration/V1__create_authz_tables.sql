-- ============================================================
-- authz-core V1: Local Authorization Schema
-- Provisioned by the authz-core library into every application
-- service database that embeds it.
-- All tables use soft deletes: deleted_at IS NULL = active.
-- ============================================================

-- 1. Protected resources (auto-registered from @PolicyResource)
CREATE TABLE IF NOT EXISTS authz_resource (
    id          BIGSERIAL PRIMARY KEY,
    namespace   VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP,
    CONSTRAINT uq_authz_resource_namespace_name UNIQUE (namespace, name)
);

-- 2. Permissions — specific actions on a resource (auto-registered)
CREATE TABLE IF NOT EXISTS authz_permission (
    id          BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL REFERENCES authz_resource(id),
    action      VARCHAR(100) NOT NULL,
    code        VARCHAR(500) NOT NULL,  -- {namespace}:{resource}:{action}
    description VARCHAR(500),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP,
    CONSTRAINT uq_authz_permission_resource_action UNIQUE (resource_id, action),
    CONSTRAINT uq_authz_permission_code            UNIQUE (code)
);

-- 3. Condition fields — attributes available in the Condition Builder UI
CREATE TABLE IF NOT EXISTS authz_condition_field (
    id               BIGSERIAL PRIMARY KEY,
    permission_id    BIGINT NOT NULL REFERENCES authz_permission(id),
    field_name       VARCHAR(255) NOT NULL,
    field_type       VARCHAR(20)  NOT NULL CHECK (field_type IN ('NUMBER','STRING','BOOLEAN','DATE')),
    display_name     VARCHAR(255),
    allowed_values   JSONB,         -- JSON array, e.g. ["EXPENSE","INCOME"]
    options_endpoint VARCHAR(500),  -- dynamic dropdown endpoint
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','DEPRECATED')),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at       TIMESTAMP,
    CONSTRAINT uq_authz_condition_field_perm_name UNIQUE (permission_id, field_name)
);

-- 4. Policies — the core authorization rules
CREATE TABLE IF NOT EXISTS authz_policy (
    id              BIGSERIAL PRIMARY KEY,
    permission_id   BIGINT       NOT NULL REFERENCES authz_permission(id),
    subject_type    VARCHAR(10)  NOT NULL CHECK (subject_type IN ('ROLE','USER')),
    subject_id      VARCHAR(255) NOT NULL,   -- role name or user ID string
    effect          VARCHAR(5)   NOT NULL CHECK (effect IN ('ALLOW','DENY')),
    expression_json JSONB,                   -- condition AST as JSON; NULL = unconditional
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    disabled_reason VARCHAR(500),            -- populated when auto-disabled
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_authz_policy_subject
    ON authz_policy (subject_type, subject_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_authz_policy_permission
    ON authz_policy (permission_id)
    WHERE deleted_at IS NULL;

-- 5. OPA bundle cache — compiled bundle.tar.gz (one row per service database)
CREATE TABLE IF NOT EXISTS authz_policy_bundle_cache (
    id          BIGSERIAL PRIMARY KEY,
    namespace   VARCHAR(255) UNIQUE NOT NULL,
    bundle_data BYTEA       NOT NULL,  -- binary gzipped tar archive
    etag        VARCHAR(64) NOT NULL,  -- MD5 hash for conditional OPA polling
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
