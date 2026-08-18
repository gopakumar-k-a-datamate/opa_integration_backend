-- =========================================================================================
-- V8: Create Hibernate Envers Audit Tables
-- =========================================================================================

CREATE TABLE IF NOT EXISTS authz_resource_audit (
    id          BIGINT NOT NULL,
    rev         INT NOT NULL REFERENCES audit.revinfo(id),
    revtype     SMALLINT,
    namespace   VARCHAR(255),
    name        VARCHAR(255),
    description VARCHAR(500),
    status      VARCHAR(20),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,
    PRIMARY KEY (id, rev)
);

CREATE TABLE IF NOT EXISTS authz_permission_audit (
    id          BIGINT NOT NULL,
    rev         INT NOT NULL REFERENCES audit.revinfo(id),
    revtype     SMALLINT,
    resource_id BIGINT,
    action      VARCHAR(100),
    code        VARCHAR(500),
    description VARCHAR(500),
    status      VARCHAR(20),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,
    PRIMARY KEY (id, rev)
);

CREATE TABLE IF NOT EXISTS authz_policy_audit (
    id                  BIGINT NOT NULL,
    rev                 INT NOT NULL REFERENCES audit.revinfo(id),
    revtype             SMALLINT,
    permission_id       BIGINT,
    subject_type        VARCHAR(10),
    subject_id          VARCHAR(255),
    effect              VARCHAR(5),
    expression_json     JSONB,
    enabled             BOOLEAN,
    disabled_reason     VARCHAR(500),
    deleted_reason      VARCHAR(255),
    deprecated          BOOLEAN,
    use_custom_rego     BOOLEAN,
    custom_rego_snippet TEXT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    deleted_at          TIMESTAMP,
    PRIMARY KEY (id, rev)
);
