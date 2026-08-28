# Database-First Migration Guide

## Context & Purpose
Our authorization framework utilizes a **Database-First** paradigm. This means that the local database tables (`authz_resource`, `authz_permission`, `authz_condition_field`) act as the absolute single source of truth for all authorization schemas. 

This guide serves as a reference for developers. It outlines exactly how to add, deprecate, or delete resources and condition fields by pairing Java code changes with the mandatory Flyway SQL migration scripts.

---

## 1. Adding a New Resource and Fields

When you build a new feature that requires authorization, you must do two things: annotate the application command and write the SQL migration to populate the database.

### 1.1. Application Layer Code (The Reference)
In your application code, you define the resource, action, and fields using annotations. **Note:** These annotations do not auto-register anything in the database; they act purely as runtime markers for the AOP interceptor (`PolicyEnforcementAspect`) to extract values and evaluate policies against OPA.

```java
// Example: Adding a new Journal Create command in the Finance module
@PolicyResource(namespace = "finance", name = "journal", action = "create")
public record CreateJournalPolicyResource(
    
    @PolicyField(displayName = "Journal Amount", type = FieldType.NUMBER)
    BigDecimal amount,

    @PolicyField(displayName = "Entry Type", type = FieldType.STRING)
    String type
) {}
```

### 1.2. Flyway SQL Migration (The Source of Truth)
You must explicitly declare this schema in a Flyway migration script (e.g., `V2__add_journal_resource.sql`) so the Admin UI knows these permissions and fields exist.

```sql
-- 1. Insert the Resource
INSERT INTO authz_resource (namespace, name, display_name, status, version, created_at) 
VALUES ('finance', 'journal', 'Finance Journal', 'ACTIVE', 0, CURRENT_TIMESTAMP);

-- 2. Insert the Permission (Action)
-- Assuming the resource ID generated above is 100 for this example
INSERT INTO authz_permission (resource_id, action, code, display_name, status, version, created_at)
VALUES (100, 'create', 'finance:journal:create', 'Create Journal', 'ACTIVE', 0, CURRENT_TIMESTAMP);

-- 3. Insert the Condition Fields
-- Assuming the permission ID generated above is 200
INSERT INTO authz_condition_field (permission_id, field_name, display_name, field_type, status, version, created_at)
VALUES 
(200, 'amount', 'Journal Amount', 'NUMBER', 'ACTIVE', 0, CURRENT_TIMESTAMP),
(200, 'type', 'Entry Type', 'STRING', 'ACTIVE', 0, CURRENT_TIMESTAMP);
```

---

## 2. Deprecating a Condition Field

If you remove a field from a Java command (e.g., removing a `status` field), you must update the database to reflect this deprecation. 

### 2.1. Flyway SQL Migration
```sql
-- V3__deprecate_status_field.sql

-- 1. Mark the field as DEPRECATED in the condition fields table
UPDATE authz_condition_field 
SET status = 'DEPRECATED' 
WHERE field_name = 'status' 
  AND permission_id IN (SELECT id FROM authz_permission WHERE code LIKE 'finance:%');

-- 2. Explicitly invalidate the OPA bundle cache
-- This forces the next read request to synchronize the deprecated flag and recompile the Rego bundle.
UPDATE authz_policy_bundle_cache 
SET etag = NULL, bundle_data = NULL;
```

### 2.2. How the Logic Works
1. **Cache Invalidation:** Setting `etag` and `bundle_data` to `NULL` triggers a system recalculation.
2. **Policy Sync:** Any existing rows in `authz_policy` that currently use the deprecated `status` field will automatically be flagged with `deprecated = true`.
3. **Runtime Impact:** To guarantee a fail-closed secure state, the policies are **excluded** from the OPA compilation pipeline. The `deprecated = true` flag drops the policy entirely from Rego generation.
4. **UI Impact:** The `deprecated = true` flag triggers a visual warning in the Admin UI, prompting the administrator to update their policies to use newer fields before the deprecated field is fully deleted in a future release.

---

## 3. Un-deprecating a Condition Field

If a field was deprecated by mistake, or you decide to restore it, you can easily reverse the action. The system will automatically detect the change and re-activate all affected policies.

### 3.1. Flyway SQL Migration
```sql
-- V4__undeprecate_status_field.sql

-- 1. Restore the field to ACTIVE in the condition fields table
UPDATE authz_condition_field 
SET status = 'ACTIVE' 
WHERE field_name = 'status' 
  AND permission_id IN (SELECT id FROM authz_permission WHERE code LIKE 'finance:%');

-- 2. Explicitly invalidate the OPA bundle cache
UPDATE authz_policy_bundle_cache 
SET etag = NULL, bundle_data = NULL;
```

### 3.2. How the Logic Works
1. **Cache Invalidation:** The `NULL` cache triggers a recompilation on the next read request.
2. **Policy Sync:** The compiler checks the active fields. Since the `status` field is no longer marked as `DEPRECATED` in the database, it automatically updates the `deprecated` flag on all referencing `authz_policy` rows back to `false`.
3. **Runtime Impact:** The policies are immediately **re-included** in the OPA compilation pipeline. They are restored to the Rego bundle and resume enforcing authorization exactly as they did before.
4. **UI Impact:** The `deprecated = true` warning banner is removed from the Admin UI.

---

## 4. Deleting a Resource

When an entire resource (and its associated permissions) is permanently removed from the application, it must be removed from the Admin UI.

### 4.1. Flyway SQL Migration
```sql
-- V4__delete_old_resource.sql

-- 1. Soft-delete the permission(s)
UPDATE authz_permission 
SET status = 'DELETED', deleted_at = CURRENT_TIMESTAMP 
WHERE resource_id = (SELECT id FROM authz_resource WHERE namespace = 'finance' AND name = 'old_resource');

-- 2. Soft-delete the resource
UPDATE authz_resource 
SET status = 'DELETED', deleted_at = CURRENT_TIMESTAMP 
WHERE namespace = 'finance' AND name = 'old_resource';

-- 3. Explicitly invalidate the OPA bundle cache
UPDATE authz_policy_bundle_cache 
SET etag = NULL, bundle_data = NULL;
```

### 4.2. How the Logic Works
1. **UI Impact:** Because the resource and permissions are marked as `DELETED`, they will no longer appear in the Admin UI. Administrators can no longer create or manage policies for this resource.
2. **Policy Impact:** **No changes** are made to the existing policies in the `authz_policy` table. 
   - **Design Decision:** We deliberately leave existing `authz_policy` rows untouched when a resource is deleted. This preserves historical configuration data and avoids destructive cascading deletes, even though the resource is no longer actively evaluated.
