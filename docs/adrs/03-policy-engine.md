# Policy Engine (Federated Library)

This document covers the condition engine, conflict resolution rules, the local field registry, and field deprecation handling within the `authz-core` library.

---

## 1. Condition Engine — JSON AST

Policies often require complex, nested conditions. The library stores the entire condition tree as an **Abstract Syntax Tree (AST)** in the `expression_json` column of the `authz_policy` table.

### Simple Condition

```json
{
  "operator": "AND",
  "children": [
    { "field": "resource.amount", "comparison": "<=", "value": 10000 },
    { "field": "resource.bank", "comparison": "!=", "value": "CASH" }
  ]
}
```

### Unconditional Policy

When `expression_json` is `NULL`, the policy applies unconditionally.

### Supported Operators by Field Type

| Field Type | Supported Comparisons |
|---|---|
| `NUMBER` | `==`, `!=`, `<`, `<=`, `>`, `>=` |
| `STRING` | `==`, `!=`, `in`, `not_in` |
| `BOOLEAN` | `==`, `!=` |
| `DATE` | `==`, `!=`, `<`, `<=`, `>`, `>=` |

---

## 2. Conflict Resolution Rules

These rules govern how OPA evaluates overlapping policies.

### Rule 1: Cross-Role Union (Most Permissive Wins)
If a user has multiple roles, they get the **union** of all role permissions.

### Rule 2: Same-Subject, Same-Permission (Most Permissive Wins)
If multiple ALLOW policies exist for the same subject and permission, the most permissive one wins.

### Rule 3: User-Level + Role-Level (Most Permissive Wins)
User-level policies are OR'd with role-level policies, allowing specific users higher limits than their role allows.

### Rule 4: DENY Overrides ALLOW (Always)
Any matching DENY policy blocks access regardless of ALL matching ALLOW policies. Enforced via `not deny_rule` in the Rego final decision.

---

## 3. Database-First Authorization Registration

We employ a **Database-First** approach to manage authorization metadata. The local database (`authz_resource`, `authz_permission`, `authz_condition_field` tables) serves as the strict, single source of truth for all authorization constructs.

Because this is a federated model, each application service manages its own subset of the authorization schema using local Flyway SQL migrations.

### Step 1: SQL-Driven Lifecycle Management (Flyway)

Any new resource, permission, or condition field is explicitly declared in a Flyway SQL script. This guarantees predictable, version-controlled metadata across all environments and completely avoids the unpredictability of automatic runtime reflection updates.

### Step 2: Code as a Reference (The AOP Interceptor)

While the database dictates the available permissions to the Admin UI, the application code must enforce them. The application layer annotates commands strictly as a reference point for the runtime policy enforcement interceptor.

```java
// In the Finance module's Application Layer
@PolicyResource(namespace = "finance", name = "journal", action = "create")
public record CreateJournalPolicyResource(
    
    @PolicyField(displayName = "Journal Amount", type = FieldType.NUMBER)
    BigDecimal amount,

    @PolicyField(displayName = "Bank Account", type = FieldType.STRING,
                 optionsEndpoint = "/api/finance/banks")
    String bank,

    @PolicyField(displayName = "Entry Type", type = FieldType.STRING,
                 allowedValues = {"EXPENSE", "INCOME", "TRANSFER"})
    String type
) {}
```

The `@PolicyResource` and `@PolicyField` annotations act purely as runtime markers. When a command is invoked, the `PolicyEnforcementAspect`:
1. Intercepts the method execution and extracts the `namespace`, `name`, and `action` from `@PolicyResource`.
2. Extracts condition context values dynamically from fields annotated with `@PolicyField`.
3. Constructs the `OpaInputPayload` and delegates the evaluation to the OPA sidecar.

---

## 4. Startup Synchronization & Bundle Recompilation

Since Flyway migrations execute during application boot, the database state (and potentially policies) may change before the application accepts traffic. 

To ensure the OPA sidecar has the most up-to-date policies, the application utilizes a `StartupPolicyCompiler` listener.

### Boot Sequence

1. The application boots and Flyway SQL migrations are applied (e.g., adding/deprecating fields).
2. On `ContextRefreshedEvent`, the `StartupPolicyCompiler` scans the database for all active namespaces.
3. It forces a complete recompilation of the Rego bundles from the current database state and pushes them to OPA.

**Benefits of Database-First:**
- **Predictable Deployments:** Migrations fail deterministically if there's a schema issue, compared to hidden runtime auto-registration failures.
- **Strict Lifecycle Control:** Deprecating or disabling a field or policy is explicit via SQL migrations (e.g., setting a `deprecated` flag), preventing accidental removals via code changes.
- **Zero Magic:** No hidden classpath scanning or complex diff-sync logic, keeping application startup fast and transparent.

---

## 5. Deprecation and Deletion Logic

Under the Database-First paradigm, lifecycle changes (deprecation and deletion) are executed strictly through Flyway SQL migrations. This ensures predictable state changes and forces cache invalidations across all environments.

### Deprecating a Condition Field
When a condition field is no longer needed, the migration script handles it as follows:
- The `authz_condition_field` is updated to `status = 'DEPRECATED'`.
- The `etag` and `bundle` columns are explicitly set to `NULL` to invalidate the bundle cache.
- Any existing policies in `authz_policy` that reference this specific condition field are updated to `deprecated = true`. 
- **Runtime Impact**: The policies are **not** disabled. They are still compiled into the OPA bundle and evaluated normally. However, the `deprecated` flag triggers a warning in the Admin UI, prompting the administrator to migrate away from the deprecated field.

### Deleting a Resource
When an entire resource is deprecated or removed from the system, it is treated as deleted from the UI's perspective:
- The `authz_resource` and its associated `authz_permission` rows are marked as deleted (soft-deleted).
- The `etag` and `bundle` caches are set to `NULL` via the migration script to force recompilation.
- **Policy Impact**: **No changes** are made to the existing policies in `authz_policy`. The explicit design decision is to leave the existing policy rows completely untouched when a parent resource is deleted. The resource and policies will simply no longer be visible or manageable via the Admin UI.
