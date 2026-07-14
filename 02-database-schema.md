# Database Schema

> All entities use soft deletes (`deleted_at` timestamp). A `NULL` value means the record is active.

---

## 1. Namespace (`permission_namespace`)
Represents a broad business domain to group resources. Auto-registered on startup.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `name` | VARCHAR | UNIQUE, NOT NULL | e.g., `finance`, `clinical`, `inventory` |
| `description` | VARCHAR | | Human-readable description |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | | |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Example:** `id: 1, name: finance`

---

## 2. Resource (`resource`)
Represents a protected entity within a namespace. Auto-registered on startup.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `namespace_id` | BIGINT | FK → `permission_namespace`, NOT NULL | |
| `name` | VARCHAR | NOT NULL | e.g., `journal`, `patient` |
| `description` | VARCHAR | | Human-readable description |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | | |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Unique constraint:** `(namespace_id, name)` — no duplicate resource names within a namespace.

**Example:** `id: 101, namespace_id: 1 (finance), name: journal`

---

## 3. Permission (`permission`)
Represents a specific action that can be performed on a resource. Auto-registered on startup.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `resource_id` | BIGINT | FK → `resource`, NOT NULL | |
| `action` | VARCHAR | NOT NULL | e.g., `create`, `view`, `delete` |
| `code` | VARCHAR | UNIQUE, NOT NULL | Auto-generated: `{namespace}:{resource}:{action}` |
| `description` | VARCHAR | | Human-readable description |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | | |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Unique constraint:** `(resource_id, action)`

**Example:** `id: 501, resource_id: 101, action: create, code: finance:journal:create`

---

## 4. Condition Field (`condition_field`) *(formerly resource_field)*
Defines attributes that can be used in policies for a specific **Permission** (Action). **Populated automatically** via reflection-based registration from Application Commands at startup (see [03-policy-engine.md](file:///Users/apple/Documents/opa_integration_backend/03-policy-engine.md#field-registry)).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `permission_id` | BIGINT | FK → `permission`, NOT NULL | Links fields directly to the action |
| `field_name` | VARCHAR | NOT NULL | e.g., `amount`, `bank` |
| `field_type` | ENUM | NOT NULL | `NUMBER`, `STRING`, `BOOLEAN`, `DATE` |
| `display_name` | VARCHAR | | e.g., `Journal Amount` (for UI labels) |
| `allowed_values` | JSON | | Static dropdowns, e.g., `["CASH", "HDFC", "SBI"]` |
| `options_endpoint` | VARCHAR | | Dynamic dropdowns, e.g., `/api/finance/banks`. UI expects `[{id, display}]` |
| `status` | ENUM | NOT NULL, default `ACTIVE` | `ACTIVE` or `DEPRECATED` |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | | |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Unique constraint:** `(permission_id, field_name)` — fields are unique per action, not per generic resource.

**Status values:**

| Status | Meaning |
|---|---|
| `ACTIVE` | Field exists in code and is available for new conditions |
| `DEPRECATED` | Field was removed from code but existing policies still reference it |

**Example fields for `finance:journal:create` permission:**

| field_name | field_type | display_name | allowed_values | options_endpoint | status |
|---|---|---|---|---|---|
| `amount` | NUMBER | Journal Amount | `null` | `null` | ACTIVE |
| `bank` | STRING | Bank Account | `null` | `/api/finance/banks` | ACTIVE |
| `type` | STRING | Entry Type | `["EXPENSE", "INCOME", "TRANSFER"]` | `null` | ACTIVE |

---

## 5. Role (`role`)
Standard role definition.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `name` | VARCHAR | UNIQUE, NOT NULL | e.g., `ACCOUNTANT`, `MANAGER` |
| `description` | VARCHAR | | Human-readable description |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | | |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Example:** `id: 901, name: ACCOUNTANT`

---

## 6. User Role (`user_role`)
Maps users to roles. A user can have multiple roles.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `user_id` | BIGINT | FK → user table, NOT NULL | |
| `role_id` | BIGINT | FK → `role`, NOT NULL | |
| `created_at` | TIMESTAMP | NOT NULL | |
| `created_by` | BIGINT | | User who assigned this role |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Unique constraint:** `(user_id, role_id)` — no duplicate role assignments.

**Purpose:** The calling service resolves `user_id → roles[]` from this table when constructing the OPA input.

---

## 7. Policy (`policy`)
The core authorization rule. **This table IS the role-to-permission mapping**, enriched with dynamic conditions.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `permission_id` | BIGINT | FK → `permission`, NOT NULL | |
| `subject_type` | ENUM | NOT NULL | `ROLE` or `USER` |
| `subject_id` | BIGINT | NOT NULL | FK to `role.id` or `user.id` depending on `subject_type` |
| `effect` | ENUM | NOT NULL | `ALLOW` or `DENY` |
| `expression_json` | JSON | Nullable | Condition AST. `NULL` = unconditional (always applies) |
| `enabled` | BOOLEAN | NOT NULL, default `true` | Toggle without deleting |
| `disabled_reason` | VARCHAR | | Set automatically when auto-disabled (e.g., `"Field 'bank' deprecated"`) |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | | |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Example policies:**

### Policy 1 — Accountant limit
| Column | Value |
|---|---|
| `permission_id` | 501 (`finance:journal:create`) |
| `subject_type` | `ROLE` |
| `subject_id` | 901 (`ACCOUNTANT`) |
| `effect` | `ALLOW` |
| `expression_json` | `{"field":"amount", "comparison":"<=", "value":10000}` |

### Policy 2 — Manager limit
| Column | Value |
|---|---|
| `permission_id` | 501 (`finance:journal:create`) |
| `subject_type` | `ROLE` |
| `subject_id` | 902 (`MANAGER`) |
| `effect` | `ALLOW` |
| `expression_json` | `{"field":"amount", "comparison":"<=", "value":20000}` |

### Policy 3 — Specific user override
| Column | Value |
|---|---|
| `permission_id` | 501 (`finance:journal:create`) |
| `subject_type` | `USER` |
| `subject_id` | 42 |
| `effect` | `ALLOW` |
| `expression_json` | `{"field":"amount", "comparison":"<=", "value":15000}` |

### Policy 4 — Specific user deny (unconditional)
| Column | Value |
|---|---|
| `permission_id` | 501 (`finance:journal:create`) |
| `subject_type` | `USER` |
| `subject_id` | 123 (John) |
| `effect` | `DENY` |
| `expression_json` | `null` |

---

## 8. Policy Bundle Cache (`policy_bundle_cache`)
Stores compiled OPA bundles. **One bundle per namespace.**

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `namespace_id` | BIGINT | FK → `permission_namespace`, UNIQUE, NOT NULL | One bundle per namespace |
| `bundle_data` | BLOB/BYTEA | NOT NULL | The compiled `bundle.tar.gz` |
| `etag` | VARCHAR | NOT NULL | MD5 hash of the bundle for conditional serving |
| `created_at` | TIMESTAMP | NOT NULL | When this bundle was generated |

**Purpose:** Ensures all application instances serve the exact same bundle to OPA without recompiling it on the fly. The `UNIQUE` constraint on `namespace_id` means each namespace has exactly one cached bundle (upserted on regeneration).
