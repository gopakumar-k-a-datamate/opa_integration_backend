-- =========================================================================
-- V1: Seed Clinical & Billing Authorization Data
-- Pre-populates the authz_ tables for clinic-modulith resources.
-- =========================================================================

-- 1. Insert Resources
INSERT INTO authz_resource (namespace, name, description) 
VALUES ('clinical', 'encounter', 'View patient encounters')
ON CONFLICT (namespace, name) WHERE deleted_at IS NULL DO UPDATE SET description = EXCLUDED.description;

INSERT INTO authz_resource (namespace, name, description) 
VALUES ('billing', 'invoice', 'Create patient invoice')
ON CONFLICT (namespace, name) WHERE deleted_at IS NULL DO UPDATE SET description = EXCLUDED.description;

-- 2. Insert Permissions
INSERT INTO authz_permission (resource_id, action, code, description) 
SELECT id, 'read', 'clinical:encounter:read', 'Read Encounter Permission' FROM authz_resource WHERE namespace='clinical' AND name='encounter'
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET description = EXCLUDED.description;

INSERT INTO authz_permission (resource_id, action, code, description) 
SELECT id, 'create', 'billing:invoice:create', 'Create Invoice Permission' FROM authz_resource WHERE namespace='billing' AND name='invoice'
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET description = EXCLUDED.description;


-- 3. Insert Condition Fields for 'clinical:encounter:read'
INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'specialty', 'STRING', 'Doctor Specialty', '["CARDIOLOGY", "NEUROLOGY", "GENERAL_PRACTICE", "PEDIATRICS"]'::jsonb
FROM authz_permission p WHERE p.code='clinical:encounter:read'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'isConfidential', 'BOOLEAN', 'Is Confidential', NULL
FROM authz_permission p WHERE p.code='clinical:encounter:read'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'patientAge', 'NUMBER', 'Patient Age', NULL
FROM authz_permission p WHERE p.code='clinical:encounter:read'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'diagnosisCode', 'STRING', 'Diagnosis Code', NULL
FROM authz_permission p WHERE p.code='clinical:encounter:read'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'encounterDate', 'DATE', 'Encounter Date', NULL
FROM authz_permission p WHERE p.code='clinical:encounter:read'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'locationId', 'STRING', 'Clinic Location', '["MAIN_CAMPUS", "NORTH_BRANCH", "SOUTH_BRANCH"]'::jsonb
FROM authz_permission p WHERE p.code='clinical:encounter:read'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;


-- 4. Insert Condition Fields for 'billing:invoice:create'
INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'totalAmount', 'NUMBER', 'Total Amount', NULL
FROM authz_permission p WHERE p.code='billing:invoice:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'insuranceProvider', 'STRING', 'Insurance Provider', '["BLUE_CROSS", "MEDICARE", "AETNA", "CIGNA", "UNINSURED"]'::jsonb
FROM authz_permission p WHERE p.code='billing:invoice:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'isPaid', 'BOOLEAN', 'Is Fully Paid', NULL
FROM authz_permission p WHERE p.code='billing:invoice:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'discountPercentage', 'NUMBER', 'Discount Percentage', NULL
FROM authz_permission p WHERE p.code='billing:invoice:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'dueDate', 'DATE', 'Due Date', NULL
FROM authz_permission p WHERE p.code='billing:invoice:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;
