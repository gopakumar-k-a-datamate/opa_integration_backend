-- Seed script for finance-microservice authorization resources

-- 1. Insert Resource
INSERT INTO authz_resource (namespace, name, description) 
VALUES ('finance', 'journal', 'Finance Journal Resource')
ON CONFLICT (namespace, name) WHERE deleted_at IS NULL DO UPDATE SET description = EXCLUDED.description;

-- 2. Insert Permissions
INSERT INTO authz_permission (resource_id, action, code, description) 
SELECT id, 'create', 'finance:journal:create', 'Create new journal entries' FROM authz_resource WHERE namespace='finance' AND name='journal'
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET description = EXCLUDED.description;

INSERT INTO authz_permission (resource_id, action, code, description) 
SELECT id, 'approve', 'finance:journal:approve', 'Approve a submitted journal entry' FROM authz_resource WHERE namespace='finance' AND name='journal'
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET description = EXCLUDED.description;

-- 3. Insert Condition Fields for 'create'
INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'amount', 'NUMBER', 'Amount', NULL 
FROM authz_permission p WHERE p.code='finance:journal:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'department', 'STRING', 'Department', '["HR", "IT", "FINANCE", "OPERATIONS"]' 
FROM authz_permission p WHERE p.code='finance:journal:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'costCenter', 'STRING', 'Cost Center', NULL 
FROM authz_permission p WHERE p.code='finance:journal:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'status', 'STRING', 'Status', '["DRAFT", "PENDING_APPROVAL", "APPROVED", "REJECTED"]' 
FROM authz_permission p WHERE p.code='finance:journal:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'requiresAudit', 'BOOLEAN', 'Requires Audit', NULL 
FROM authz_permission p WHERE p.code='finance:journal:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'transactionDate', 'DATE', 'Transaction Date', NULL 
FROM authz_permission p WHERE p.code='finance:journal:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;


-- 4. Insert Condition Fields for 'approve'
INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'amount', 'NUMBER', 'Amount', NULL 
FROM authz_permission p WHERE p.code='finance:journal:approve'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'department', 'STRING', 'Department', '["HR", "IT", "FINANCE", "OPERATIONS"]' 
FROM authz_permission p WHERE p.code='finance:journal:approve'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'requiresAudit', 'BOOLEAN', 'Requires Audit', NULL 
FROM authz_permission p WHERE p.code='finance:journal:approve'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;
