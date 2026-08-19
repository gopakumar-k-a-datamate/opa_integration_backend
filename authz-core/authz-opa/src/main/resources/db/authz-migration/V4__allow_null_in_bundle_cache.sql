-- V4_1__allow_null_in_bundle_cache.sql

-- Drop NOT NULL constraints on bundle_data and etag to support explicit cache invalidation
ALTER TABLE authz_policy_bundle_cache ALTER COLUMN bundle_data DROP NOT NULL;
ALTER TABLE authz_policy_bundle_cache ALTER COLUMN etag DROP NOT NULL;
