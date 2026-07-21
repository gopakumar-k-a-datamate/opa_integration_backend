-- =========================================================================
-- V1: Seed Clinical & Billing Authorization Data
-- Pre-populates the authz_ tables for clinic-modulith resources.
-- =========================================================================

-- 1. Insert Resources
INSERT INTO authz_resource (id, namespace, name, description) VALUES
(1, 'clinical', 'encounter', 'View patient encounters'),
(2, 'billing', 'invoice', 'Create patient invoice');

ALTER SEQUENCE authz_resource_id_seq RESTART WITH 3;

-- 2. Insert Permissions
INSERT INTO authz_permission (id, resource_id, action, code, description) VALUES
(1, 1, 'read', 'clinical:encounter:read', 'Read Encounter Permission'),
(2, 2, 'create', 'billing:invoice:create', 'Create Invoice Permission');

ALTER SEQUENCE authz_permission_id_seq RESTART WITH 3;

-- 3. Insert Condition Fields
-- For clinical:encounter:read (permission_id = 1)
INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values) VALUES
(1, 'specialty', 'STRING', 'Doctor Specialty', '["CARDIOLOGY", "NEUROLOGY", "GENERAL_PRACTICE", "PEDIATRICS"]'::jsonb),
(1, 'isConfidential', 'BOOLEAN', 'Is Confidential', NULL),
(1, 'patientAge', 'NUMBER', 'Patient Age', NULL),
(1, 'diagnosisCode', 'STRING', 'Diagnosis Code', NULL),
(1, 'encounterDate', 'DATE', 'Encounter Date', NULL),
(1, 'locationId', 'STRING', 'Clinic Location', '["MAIN_CAMPUS", "NORTH_BRANCH", "SOUTH_BRANCH"]'::jsonb);

-- For billing:invoice:create (permission_id = 2)
INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, allowed_values) VALUES
(2, 'totalAmount', 'NUMBER', 'Total Amount', NULL),
(2, 'insuranceProvider', 'STRING', 'Insurance Provider', '["BLUE_CROSS", "MEDICARE", "AETNA", "CIGNA", "UNINSURED"]'::jsonb),
(2, 'isPaid', 'BOOLEAN', 'Is Fully Paid', NULL),
(2, 'discountPercentage', 'NUMBER', 'Discount Percentage', NULL),
(2, 'dueDate', 'DATE', 'Due Date', NULL);

