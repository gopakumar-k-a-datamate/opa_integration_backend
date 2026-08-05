-- =========================================================================================
-- V4_2: Convert Global Unique Constraints to Partial Indexes
-- =========================================================================================
-- Purpose: 
-- Because we rely on soft-deletes (deleted_at IS NOT NULL) to deprecate resources,
-- permissions, and condition fields, the global UNIQUE constraints prevent us from
-- ever reusing a namespace, name, or code in the future.
-- 
-- This script drops the strict table constraints and replaces them with 
-- Partial Unique Indexes that only enforce uniqueness on ACTIVE (non-deleted) rows.
-- =========================================================================================

-- 1. Drop the existing global constraints
ALTER TABLE authz_resource 
    DROP CONSTRAINT uq_authz_resource_namespace_name;

ALTER TABLE authz_permission 
    DROP CONSTRAINT uq_authz_permission_resource_action,
    DROP CONSTRAINT uq_authz_permission_code;

ALTER TABLE authz_condition_field 
    DROP CONSTRAINT uq_authz_condition_field_perm_name;

-- 2. Recreate them as Partial Indexes ignoring soft-deleted rows
CREATE UNIQUE INDEX uq_authz_resource_namespace_name 
    ON authz_resource (namespace, name) 
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_authz_permission_resource_action 
    ON authz_permission (resource_id, action) 
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_authz_permission_code 
    ON authz_permission (code) 
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_authz_condition_field_perm_name 
    ON authz_condition_field (permission_id, field_name) 
    WHERE deleted_at IS NULL;
