-- V2__deprecate_status_field.sql

-- 1. Deprecate the 'status' condition field for the finance module
-- We use field_name='status' and ensure it belongs to the finance module (though field_name is globally unique per permission, doing it globally for 'status' is fine for this test).
UPDATE authz_condition_field 
SET status = 'DEPRECATED' 
WHERE field_name = 'status' 
  AND permission_id IN (SELECT id FROM authz_permission WHERE code LIKE 'finance:%');

-- 2. Explicitly invalidate the OPA bundle cache
-- This forces the next read request to synchronize the deprecated flag on all policies and recompile the Rego.
UPDATE authz_policy_bundle_cache 
SET etag = NULL, bundle_data = NULL;
