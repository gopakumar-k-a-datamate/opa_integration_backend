-- Seed data for pharmacy prescription resources

INSERT INTO authz_resource (id, namespace, name, description)
VALUES (2, 'pharmacy', 'prescription', 'Pharmacy Prescription Resource')
ON CONFLICT DO NOTHING;

INSERT INTO authz_permission (id, resource_id, action, code, description)
VALUES (2, 2, 'create', 'pharmacy:prescription:create', 'Create Prescription')
ON CONFLICT DO NOTHING;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
VALUES 
(2, 'doctorLevel', 'STRING', 'Doctor Level', '["MAIN", "SENIOR", "JUNIOR"]'),
(2, 'isSameWard', 'BOOLEAN', 'Is Same Ward', '["true", "false"]');

-- Note: We don't insert a default policy here so you can demonstrate creating it in the Admin UI!
