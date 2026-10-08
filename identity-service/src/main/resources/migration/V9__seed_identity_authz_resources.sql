-- ============================================================
-- V9: Seed Identity Authorization Resources & Permissions
-- Namespace: identity
-- ============================================================

-- 1. Insert Identity Resources
INSERT INTO authz_resource (id, namespace, name, description, status)
VALUES 
    (1, 'identity', 'user', 'Identity User Management Resource', 'ACTIVE'),
    (2, 'identity', 'role', 'Identity Role Management Resource', 'ACTIVE')
ON CONFLICT (namespace, name) DO UPDATE 
SET description = EXCLUDED.description,
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP;

-- 2. Insert Identity Permissions
INSERT INTO authz_permission (id, resource_id, action, code, description, status)
VALUES 
    (1, 1, 'read',   'identity:user:read',   'View Users',                'ACTIVE'),
    (2, 1, 'create', 'identity:user:create', 'Create New User',           'ACTIVE'),
    (3, 1, 'update', 'identity:user:update', 'Update User Details',       'ACTIVE'),
    (4, 1, 'delete', 'identity:user:delete', 'Deactivate or Delete User', 'ACTIVE'),
    (5, 2, 'read',   'identity:role:read',   'View Roles',                'ACTIVE'),
    (6, 2, 'create', 'identity:role:create', 'Create New Role',           'ACTIVE'),
    (7, 2, 'assign', 'identity:role:assign', 'Assign Roles to Users',     'ACTIVE')
ON CONFLICT (code) DO UPDATE 
SET description = EXCLUDED.description,
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP;

-- 3. Synchronize Postgres Sequences for generated IDs
SELECT setval('authz_resource_id_seq', (SELECT COALESCE(MAX(id), 1) FROM authz_resource));
SELECT setval('authz_permission_id_seq', (SELECT COALESCE(MAX(id), 1) FROM authz_permission));
