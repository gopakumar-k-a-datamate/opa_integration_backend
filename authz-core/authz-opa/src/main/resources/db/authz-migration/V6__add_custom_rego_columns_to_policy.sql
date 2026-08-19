-- V6: Add custom Rego support to authz_policy

ALTER TABLE authz_policy
    ADD COLUMN use_custom_rego BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE authz_policy
    ADD COLUMN custom_rego_snippet TEXT;

ALTER TABLE authz_policy
    ADD CONSTRAINT chk_custom_rego_consistency
    CHECK (
        (use_custom_rego = FALSE)
        OR (use_custom_rego = TRUE AND custom_rego_snippet IS NOT NULL)
    );
