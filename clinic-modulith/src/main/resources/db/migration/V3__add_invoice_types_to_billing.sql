-- =========================================================================
-- V3: Add invoiceType and accountType to billing:invoice:create and update
-- =========================================================================

-- 1. Add condition fields for 'billing:invoice:create'
INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'invoiceType', 'STRING', 'Invoice Type', '["OUTPATIENT", "INPATIENT", "PHARMACY", "EMERGENCY"]'::jsonb
FROM authz_permission p WHERE p.code='billing:invoice:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'accountType', 'STRING', 'Account Type', '["INDIVIDUAL_DEBTOR", "CORPORATE_DEBTOR", "INSURANCE_CLAIM", "CASH_ACCOUNT"]'::jsonb
FROM authz_permission p WHERE p.code='billing:invoice:create'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;


-- 2. Add condition fields for 'billing:invoice:update'
INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'invoiceType', 'STRING', 'Invoice Type', '["OUTPATIENT", "INPATIENT", "PHARMACY", "EMERGENCY"]'::jsonb
FROM authz_permission p WHERE p.code='billing:invoice:update'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;

INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values)
SELECT p.id, 'accountType', 'STRING', 'Account Type', '["INDIVIDUAL_DEBTOR", "CORPORATE_DEBTOR", "INSURANCE_CLAIM", "CASH_ACCOUNT"]'::jsonb
FROM authz_permission p WHERE p.code='billing:invoice:update'
ON CONFLICT (permission_id, field_name) WHERE deleted_at IS NULL DO UPDATE SET display_name=EXCLUDED.display_name, allowed_values=EXCLUDED.allowed_values;
