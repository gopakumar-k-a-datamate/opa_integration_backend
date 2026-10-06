-- V11: Add denial_message to authz_policy
-- Optional field: admins set a human-readable reason shown to users when this policy denies access.
-- The Rego compiler bakes this message into the OPA bundle at compile time.
-- NULL = fall back to generic "Access Denied" message at runtime.

ALTER TABLE authz_policy
    ADD COLUMN denial_message VARCHAR(500);
