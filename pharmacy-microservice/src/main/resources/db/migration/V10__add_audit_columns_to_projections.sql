-- ============================================================
-- V10: Add Auditing Columns to Projections
-- Adds fields required for out-of-band drift reconciliation (ADR-007)
-- ============================================================

ALTER TABLE projection_user ADD COLUMN IF NOT EXISTS last_processed_version BIGINT NOT NULL DEFAULT 1;
ALTER TABLE projection_user ADD COLUMN IF NOT EXISTS checksum VARCHAR(32);
ALTER TABLE projection_user ADD COLUMN IF NOT EXISTS last_reconciled_at TIMESTAMP;

ALTER TABLE projection_role ADD COLUMN IF NOT EXISTS last_processed_version BIGINT NOT NULL DEFAULT 1;
ALTER TABLE projection_role ADD COLUMN IF NOT EXISTS checksum VARCHAR(32);
ALTER TABLE projection_role ADD COLUMN IF NOT EXISTS last_reconciled_at TIMESTAMP;
