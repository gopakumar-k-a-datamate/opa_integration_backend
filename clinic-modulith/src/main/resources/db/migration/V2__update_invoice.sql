INSERT INTO authz_permission (resource_id, action, code, description)
SELECT id, 'update', 'billing:invoice:update', 'Update Invoice Permission' FROM authz_resource WHERE namespace='billing' AND name='invoice'
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET description = EXCLUDED.description;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'isPaid', 'BOOLEAN', 'Is Fully Paid', NULL
FROM authz_permission p WHERE p.code='billing:invoice:update'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'totalAmount', 'NUMBER', 'Total Amount', NULL
FROM authz_permission p WHERE p.code='billing:invoice:update'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;