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

## 3. Local Registration Strategy (100% Auto-Registration)

Because this is a federated model, **each application service registers its own resources purely locally**. 

Everything is **100% auto-registered on startup** from the `@PolicyResource` annotations into the local database schema. 

### Step 1: Annotate Application Layer Commands

A module (e.g., Finance) annotates its Commands with `@PolicyResource(namespace="finance", name="journal", action="create")`. The `namespace` acts as the bounded context.

```java
// In the Finance module's Application Layer
@PolicyResource(namespace = "finance", name = "journal", action = "create")
public record CreateJournalCommand(
    
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

### Step 2: Local Startup Scan & Upsert

At application startup, the `authz-core` library:
1. Scans the local classpath for all `@PolicyResource` annotations.
2. Builds a complete graph of its Resources, Actions (Permissions), and Condition Fields.
3. Performs an **upsert** directly against the local `authz_*` tables.

**Benefits:**
- **Zero central bottlenecks:** Teams do not need to register resources with the Identity IdP.
- **Microservice-ready:** When a module is extracted to a microservice, it brings the library and its DB tables with it.

---

## 4. Diff-Based Sync — Handling Field Changes

Because the registration is 100% local, the library only diff-syncs its own code against its own tables. There is no risk of clobbering another team's data.

On each startup, the library compares incoming fields against the local `authz_condition_field` table:

| Scenario | Action |
|---|---|
| Field in code, **not** in DB | **INSERT** — new field, status = `ACTIVE` |
| Field in code **and** in DB | **UPDATE** — refresh metadata (e.g., display name), status = `ACTIVE` |
| Field in DB, **not** in code | **Check for policy references** → see below |

### When a Field Is Removed from Code

```java
Set<String> incomingFields = registration.getFieldNames();
Set<String> existingFields = conditionFieldRepo.findActiveByPermissionId(permissionId);

for (String removedField : existingFields - incomingFields) {
    List<Policy> affectedPolicies = policyRepo.findEnabledByFieldReference(
        permissionId, removedField
    );

    if (!affectedPolicies.isEmpty()) {
        // Field is still referenced — deprecate, don't delete
        conditionFieldRepo.markDeprecated(removedField);

        // Auto-disable all policies that reference this field
        for (Policy policy : affectedPolicies) {
            policy.setEnabled(false);
            policy.setDisabledReason("Field '" + removedField + "' was removed from code");
            policyRepo.save(policy);
        }
    } else {
        // No policies reference it — safe to soft delete
        conditionFieldRepo.softDelete(removedField);
    }
}
```

### What Happens to Auto-Disabled Policies

1. The policy is excluded from the next bundle compilation (no longer affects runtime authz).
2. The Admin UI shows a warning for this module.
3. The admin must either update the condition to use a different field, or delete the policy.
