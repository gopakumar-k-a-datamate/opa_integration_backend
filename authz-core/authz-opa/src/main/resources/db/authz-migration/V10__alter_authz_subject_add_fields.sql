-- ============================================================
-- authz-core V10: Extend authz_subject with richer subject fields
-- Adds unified fields that are common to both USER and ROLE subjects.
-- Fields nullable where not applicable to a subject type (e.g. email for ROLE).
-- ============================================================

-- Add display_name: human-readable label for the subject
-- USER: "John Doe" (firstName + lastName)
-- ROLE: same as subject_name (the role code/name)
ALTER TABLE authz_subject
    ADD COLUMN IF NOT EXISTS display_name VARCHAR(255) NULL;

-- Add email: only applicable for USER subjects; NULL for ROLE
ALTER TABLE authz_subject
    ADD COLUMN IF NOT EXISTS email VARCHAR(255) NULL;

-- Add description: only applicable for ROLE subjects; NULL for USER
ALTER TABLE authz_subject
    ADD COLUMN IF NOT EXISTS description VARCHAR(500) NULL;

-- Add status: mirrors the identity-service status for the subject (ACTIVE, INACTIVE)
-- Provides a richer status field alongside the soft-delete deleted_at pattern
ALTER TABLE authz_subject
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE';

-- Fix updated_at: ensure NOT NULL constraint is consistent with the entity mapping
ALTER TABLE authz_subject
    ALTER COLUMN updated_at SET NOT NULL;

-- Add partial index for fast email lookup on USER subjects
CREATE INDEX IF NOT EXISTS idx_authz_subject_email
    ON authz_subject (email)
    WHERE subject_type = 'USER' AND email IS NOT NULL;
