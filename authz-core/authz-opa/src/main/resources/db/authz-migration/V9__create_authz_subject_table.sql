-- ============================================================
-- authz-core V9: Subject Sync Table
-- Replicated from identity-service via RabbitMQ fanout events.
-- One row per (subject_type, subject_id) per consumer service DB.
-- Follows the same soft-delete pattern as all authz_* tables:
--   deleted_at IS NULL = active
--   deleted_at IS NOT NULL = deactivated / soft-deleted
-- ============================================================
CREATE TABLE IF NOT EXISTS authz_subject (
    id              BIGSERIAL    PRIMARY KEY,
    subject_type    VARCHAR(50)  NOT NULL,                              -- 'ROLE', 'USER', or future types. Validated in app code, not DB.
    subject_id      VARCHAR(255) NOT NULL,                              -- IdP-issued UUID or string identifier
    subject_name    VARCHAR(255) NOT NULL,                              -- userName for USER, role name for ROLE
    version         BIGINT       NOT NULL DEFAULT 0,                    -- Guards out-of-order event delivery
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- When this row was first created locally
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- When this row was last updated locally
    synced_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- When the latest identity-service event was applied
    deleted_at      TIMESTAMP                                           -- NULL = active. Timestamp = deactivated (soft-delete)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_authz_subject_type_id
    ON authz_subject (subject_type, subject_id);

CREATE INDEX IF NOT EXISTS idx_authz_subject_type_active
    ON authz_subject (subject_type)
    WHERE deleted_at IS NULL;
