-- ============================================================
-- V8: Identity Service Authorization Schema & Seed Data
-- ============================================================

-- 1. Protected resources
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

-- 2. Permissions — specific actions on a resource
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

-- 3. Condition fields — attributes available in Condition Builder
CREATE TABLE IF NOT EXISTS authz_condition_field (
    id               BIGSERIAL PRIMARY KEY,
    permission_id    BIGINT NOT NULL REFERENCES authz_permission(id),
    field_name       VARCHAR(255) NOT NULL,
    field_type       VARCHAR(20)  NOT NULL CHECK (field_type IN ('NUMBER','STRING','BOOLEAN','DATE')),
    display_name     VARCHAR(255),
    allowed_values   JSONB,         -- JSON array, e.g. ["ENGINEERING","SALES"]
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

-- 5. OPA bundle cache — compiled bundle.tar.gz for identity namespace
CREATE TABLE IF NOT EXISTS authz_policy_bundle_cache (
    id          BIGSERIAL PRIMARY KEY,
    namespace   VARCHAR(255) UNIQUE NOT NULL,
    bundle_data BYTEA       NOT NULL,  -- binary gzipped tar archive
    etag        VARCHAR(64) NOT NULL,  -- MD5 hash for conditional OPA polling
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 6. SEED DATA: Identity Resources & Permissions
-- ============================================================

INSERT INTO authz_resource (namespace, name, description)
VALUES 
    ('identity', 'user', 'User Management Operations'),
    ('identity', 'role', 'Role Management Operations')
ON CONFLICT (namespace, name) DO NOTHING;

-- User Permissions
INSERT INTO authz_permission (resource_id, action, code, description)
VALUES
    ((SELECT id FROM authz_resource WHERE namespace = 'identity' AND name = 'user'), 'create', 'identity:user:create', 'Create a new user account'),
    ((SELECT id FROM authz_resource WHERE namespace = 'identity' AND name = 'user'), 'read',   'identity:user:read',   'View user profiles and listings'),
    ((SELECT id FROM authz_resource WHERE namespace = 'identity' AND name = 'user'), 'update', 'identity:user:update', 'Update existing user profile details'),
    ((SELECT id FROM authz_resource WHERE namespace = 'identity' AND name = 'user'), 'delete', 'identity:user:delete', 'Deactivate or delete user account'),
    ((SELECT id FROM authz_resource WHERE namespace = 'identity' AND name = 'user'), 'unlock', 'identity:user:unlock', 'Unlock a locked user account')
ON CONFLICT (code) DO NOTHING;

-- Role Permissions
INSERT INTO authz_permission (resource_id, action, code, description)
VALUES
    ((SELECT id FROM authz_resource WHERE namespace = 'identity' AND name = 'role'), 'create', 'identity:role:create', 'Create a new custom domain role'),
    ((SELECT id FROM authz_resource WHERE namespace = 'identity' AND name = 'role'), 'read',   'identity:role:read',   'View role definitions and permissions'),
    ((SELECT id FROM authz_resource WHERE namespace = 'identity' AND name = 'role'), 'update', 'identity:role:update', 'Update existing role properties'),
    ((SELECT id FROM authz_resource WHERE namespace = 'identity' AND name = 'role'), 'assign', 'identity:role:assign', 'Assign roles to user accounts')
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- 7. SEED DATA: Condition Fields (ABAC Attributes)
-- ============================================================

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
VALUES
    ((SELECT id FROM authz_permission WHERE code = 'identity:user:create'), 'targetDepartment', 'STRING', 'Target Department', '["CLINICAL", "PHARMACY", "DENTISTRY", "FINANCE", "ADMINISTRATION"]'::jsonb),
    ((SELECT id FROM authz_permission WHERE code = 'identity:user:update'), 'targetDepartment', 'STRING', 'Target Department', '["CLINICAL", "PHARMACY", "DENTISTRY", "FINANCE", "ADMINISTRATION"]'::jsonb),
    ((SELECT id FROM authz_permission WHERE code = 'identity:role:assign'), 'isSystemRole', 'BOOLEAN', 'Is System Role', NULL),
    ((SELECT id FROM authz_permission WHERE code = 'identity:role:assign'), 'roleLevel', 'NUMBER', 'Role Security Level', NULL)
ON CONFLICT (permission_id, field_name) DO NOTHING;

-- ============================================================
-- 8. Seed Root Roles with is_system = true
-- ============================================================
INSERT INTO role (id, name, description, status, is_system, version, domain_version, created_at, updated_at)
VALUES 
    ('00000000-0000-0000-0000-000000000000', 'POLICY_ADMIN', 'Root Policy Administrator capable of authoring OPA policies', 'ACTIVE', true, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000001', 'SECURITY_ADMIN', 'Security Administrator with system governance rights', 'ACTIVE', true, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET is_system = true, status = 'ACTIVE';
