# Database Schema (Federated Model)

> All entities use soft deletes (`deleted_at` timestamp). A `NULL` value means the record is active.

This architecture is divided into two distinct schemas: the **Identity Provider Schema** (centralized) and the **Local Authorization Schema** (deployed to every application via the `authz-core` library).

---

## PART 1: Identity Provider Schema (Central)

This schema is exclusively owned by the central Identity Module.

### 1. Role (`role`)
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

### 2. User Role (`user_role`)
Maps users to roles. A user can have multiple roles.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `user_id` | BIGINT | FK → user table, NOT NULL | |
| `role_id` | BIGINT | FK → `role`, NOT NULL | |
| `created_at` | TIMESTAMP | NOT NULL | |
| `created_by` | BIGINT | | User who assigned this role |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Unique constraint:** `(user_id, role_id)`

---

## PART 2: Local Authorization Schema (via `authz-core` Library)

This schema is automatically provisioned inside the local database of *every* application service (e.g., Finance DB, Clinical DB) by the `authz-core` library.

### 1. Resource (`authz_resource`)
Represents a protected entity within this specific application/module. Auto-registered on startup.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `namespace`| VARCHAR | NOT NULL | Bounded context, e.g., `sales`, `tickets` |
| `name` | VARCHAR | NOT NULL | e.g., `customer`, `journal` |
| `description` | VARCHAR | | Human-readable description |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | | |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Unique constraint:** `(namespace, name)`
**Example:** `id: 101, namespace: sales, name: customer`

---

### 2. Permission (`authz_permission`)
Represents a specific action that can be performed on a local resource. Auto-registered on startup.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `resource_id` | BIGINT | FK → `authz_resource`, NOT NULL | |
| `action` | VARCHAR | NOT NULL | e.g., `create`, `view`, `delete` |
| `code` | VARCHAR | UNIQUE, NOT NULL | Auto-generated: `{namespace}:{resource}:{action}`, e.g., `sales:customer:create` |
| `description` | VARCHAR | | Human-readable description |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | | |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Unique constraint:** `(resource_id, action)`
**Example:** `id: 501, resource_id: 101, action: create, code: journal:create`

---

### 3. Condition Field (`authz_condition_field`)
Defines attributes that can be used in policies for a specific **Permission**. Auto-registered from Application Commands at startup (see [03-policy-engine.md](file:///Users/apple/Documents/opa_integration_backend/03-policy-engine.md)).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `permission_id` | BIGINT | FK → `authz_permission`, NOT NULL | Links fields directly to the action |
| `field_name` | VARCHAR | NOT NULL | e.g., `amount`, `bank` |
| `field_type` | ENUM | NOT NULL | `NUMBER`, `STRING`, `BOOLEAN`, `DATE` |
| `display_name` | VARCHAR | | e.g., `Journal Amount` |
| `allowed_values` | JSON | | Static dropdowns, e.g., `["CASH", "HDFC", "SBI"]` |
| `options_endpoint` | VARCHAR | | Dynamic dropdowns, e.g., `/api/finance/banks`. UI expects `[{id, display}]` |
| `status` | ENUM | NOT NULL, default `ACTIVE` | `ACTIVE` or `DEPRECATED` |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | | |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Unique constraint:** `(permission_id, field_name)`

---

### 4. Policy (`authz_policy`)
The core authorization rule. This maps global subjects (Roles/Users from the IdP) to local permissions.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `permission_id` | BIGINT | FK → `authz_permission`, NOT NULL | |
| `subject_type` | ENUM | NOT NULL | `ROLE` or `USER` |
| `subject_id` | VARCHAR | NOT NULL | e.g., `"ACCOUNTANT"` (Role Name) or `"42"` (User ID). Treated as a string reference since the actual Role lives in the IdP DB. |
| `effect` | ENUM | NOT NULL | `ALLOW` or `DENY` |
| `expression_json` | JSON | Nullable | Condition AST. `NULL` = unconditional |
| `enabled` | BOOLEAN | NOT NULL, default `true` | Toggle without deleting |
| `disabled_reason` | VARCHAR | | Set automatically when auto-disabled |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | | |
| `deleted_at` | TIMESTAMP | | Soft delete marker |

**Example Policies:**
- `permission_id`: 501, `subject_type`: ROLE, `subject_id`: "ACCOUNTANT", `effect`: ALLOW, `expression_json`: `{"field":"amount", "comparison":"<=", "value":10000}`
- `permission_id`: 501, `subject_type`: USER, `subject_id`: "123", `effect`: DENY, `expression_json`: `null`

---

### 5. Policy Bundle Cache (`authz_policy_bundle_cache`)
Stores the compiled OPA bundles for this application service, separated by namespace.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, auto-generated | |
| `namespace` | VARCHAR | UNIQUE, NOT NULL | The bounded context this bundle represents |
| `bundle_data` | BLOB/BYTEA | NOT NULL | The compiled `bundle.tar.gz` |
| `etag` | VARCHAR | NOT NULL | MD5 hash of the bundle for conditional serving |
| `created_at` | TIMESTAMP | NOT NULL | When this bundle was generated |

*(Note: There is exactly one row in this table per namespace, updated whenever local policies for that namespace change).*
