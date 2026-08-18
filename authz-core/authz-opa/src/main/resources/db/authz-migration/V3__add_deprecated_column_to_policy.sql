-- V3__add_deprecated_column_to_policy.sql

ALTER TABLE authz_policy
ADD COLUMN deprecated BOOLEAN NOT NULL DEFAULT FALSE;
