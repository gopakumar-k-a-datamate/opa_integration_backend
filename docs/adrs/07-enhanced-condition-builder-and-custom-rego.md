# Enhanced Condition Builder & Dynamic Custom Rego — Implementation Plan

This document covers two phases of backend enhancements to the `authz-core` system:

- **Phase 1**: Extend the condition builder with `IN`, `NOT_IN`, `CONTAINS`, `NOT` operators
- **Phase 2**: Add per-policy Dynamic Custom Rego with deprecation safeguards

---

## Current Architecture Summary

```
authz-core (domain + ports + DTOs + compiler)
  ├── model/policy/entity/       → Domain entities (Policy, ConditionField, etc.)
  ├── api/policy/                → Port interfaces (PolicyRepository, etc.)
  ├── dto/policy/                → DTOs (PolicyItemRequest, PolicyGridItemDto, etc.)
  ├── compiler/                  → AstBuilder, AST nodes
  └── compiler/generator/        → RegoGenerator

authz-opa (JPA persistence)
  ├── jpa/entity/                → JPA entities (PolicyJpaEntity, etc.)
  ├── jpa/repository/            → Spring Data repositories
  ├── jpa/service/               → Persistence adapters (JpaPolicyRepository, etc.)
  ├── jpa/mapper/                → Persistence mappers
  └── resources/db/authz-migration/ → Flyway SQL migrations (V1–V5)

authz-rest (REST controllers)
  └── adapter/in/rest/controller/ → PolicyController, NamespaceController, etc.
```

---

# Phase 1: Enhanced Condition Builder Operators

## Goal

Add `IN`, `NOT_IN`, `CONTAINS`, and `NOT` (group negation) support to the backend compiler so the condition builder UI can express richer policies without needing raw Rego.

## Current State

| Component | Current Support |
|---|---|
| `LogicalOperator.java` | `AND`, `OR` only |
| `ConditionNode.java` | Stores `comparison` as free-form string, `value` as single `JsonNode` |
| `RegoGenerator.java` | Emits `input.resource.<field> <comparison> <value>` literally — only works for infix operators (`==`, `!=`, `<`, `>`, `<=`, `>=`) |
| Admin UI `ConditionBuilder.jsx` | Operators dropdown: `==`, `!=`, `<=`, `>=`, `<`, `>` — value input is always single scalar |

---

## Proposed Changes

### 1.1 AST Layer

#### [MODIFY] `LogicalOperator.java`

Add `NOT` operator:

```diff
 public enum LogicalOperator {
     AND,
-    OR
+    OR,
+    NOT
 }
```

#### [MODIFY] `AstBuilder.java`

- Accept `NOT` as a valid operator in group nodes.
- Validate that `NOT` groups contain exactly one child.
- Add **depth limit** and **DNF clause limit** to prevent combinatorial explosion.

```java
private static final int MAX_AST_DEPTH = 5;
private static final int MAX_DNF_CLAUSES = 50;

// During NOT group parsing
if (operator == LogicalOperator.NOT) {
    if (childrenNode.size() != 1) {
        throw new InvalidPayloadException(
            "Invalid AST: NOT group must have exactly one child.");
    }
}

// During recursive tree parsing
if (depth > MAX_AST_DEPTH) {
    throw new InvalidPayloadException(
        "Condition tree exceeds maximum depth of " + MAX_AST_DEPTH);
}
```

> **Why the depth limit?** DNF expansion is combinatorial. `(A OR B) AND (C OR D) AND ... (N times)` produces `2^N` rules. Without a limit, a deeply nested tree causes CPU spikes or `OutOfMemoryError`. See [08-custom-rego-edge-cases-and-challenges.md](./08-custom-rego-edge-cases-and-challenges.md#26-dnf-combinatorial-explosion-phase-1-) for details.

#### No changes to `ConditionNode.java`

The `comparison` field already accepts any string, and `value` is already a `JsonNode` (can be scalar or array). No structural changes needed.

---

### 1.2 Rego Generator

#### [MODIFY] `RegoGenerator.java`

**Three areas of change:**

**A. Add `import rego.v1` to file header**

The `in` keyword requires an explicit import in OPA. Add this to the generated file header:

```java
sb.append("package app.authz.").append(namespace).append("\n\n");
sb.append("import rego.v1\n\n");  // enables: in, if, every, contains keywords
```

> **Why?** Without `import rego.v1` (or `import future.keywords.in`), OPA treats `in` as an undefined reference and the entire bundle fails. See [08-custom-rego-edge-cases-and-challenges.md](./08-custom-rego-edge-cases-and-challenges.md#31-rego-in-keyword-requires-import-) for details.

**B. New `generateCondition()` method — handle special comparison operators**

Current code emits all comparisons as infix: `input.resource.field <op> <value>`. This must be extended:

```java
private void generateCondition(ConditionNode cond, StringBuilder sb) {
    String field = cond.getField();
    String comparison = cond.getComparison();
    JsonNode value = cond.getValue();

    switch (comparison) {
        case "in" -> {
            // input.resource.department in {"Cardiology", "Neurology"}
            sb.append("    input.resource.").append(field)
              .append(" in ").append(formatSetValue(value)).append("\n");
        }
        case "not_in" -> {
            // not input.resource.department in {"Cardiology", "Neurology"}
            sb.append("    not input.resource.").append(field)
              .append(" in ").append(formatSetValue(value)).append("\n");
        }
        case "contains" -> {
            // contains(input.resource.description, "urgent")
            sb.append("    contains(input.resource.").append(field)
              .append(", ").append(formatValue(value)).append(")\n");
        }
        default -> {
            // Standard infix: input.resource.field == "value"
            sb.append("    input.resource.").append(field)
              .append(" ").append(comparison).append(" ")
              .append(formatValue(value)).append("\n");
        }
    }
}
```

**B. New `formatSetValue()` method — format JSON arrays as Rego sets**

```java
private String formatSetValue(JsonNode value) {
    if (!value.isArray()) {
        return "{" + formatValue(value) + "}";
    }
    StringBuilder sb = new StringBuilder("{");
    for (int i = 0; i < value.size(); i++) {
        if (i > 0) sb.append(", ");
        sb.append(formatValue(value.get(i)));
    }
    sb.append("}");
    return sb.toString();
}
```

**C. `NOT` group handling — emit Rego helper rules**

When a `NOT` group is encountered, the generator creates a **helper rule** and negates it.

**Example input JSON:**
```json
{
  "operator": "AND",
  "children": [
    { "field": "department", "comparison": "==", "value": "Cardiology" },
    {
      "operator": "NOT",
      "children": [
        {
          "operator": "AND",
          "children": [
            { "field": "status", "comparison": "==", "value": "DISCHARGED" },
            { "field": "priority", "comparison": "==", "value": "LOW" }
          ]
        }
      ]
    }
  ]
}
```

**Generated Rego:**
```rego
# Policy ID: 1
allow_rule if {
    "DOCTOR" in input.user.roles
    input.permission == "clinic:visit:create"
    input.resource.department == "Cardiology"
    not _not_block_p1_0
}

_not_block_p1_0 if {
    input.resource.status == "DISCHARGED"
    input.resource.priority == "LOW"
}
```

The helper rule `_not_block_p1_0` is **only referenced** from Policy 1's block. Other policies are completely unaffected. The unique naming with policy ID ensures no collisions.

**Implementation approach:**

A new data structure to hold deferred helper rules:

```java
private static class NotBlock {
    final String helperName;           // e.g., "_not_block_p1_0"
    final List<ConditionNode> conditions;
}
```

During `convertToDNF()`, when a `NOT` group is encountered:
- Generate a unique helper name
- Store the child conditions as a `NotBlock`
- Insert a placeholder in the DNF clause

After emitting the main rule, emit all collected `NotBlock` helper rules.

---

### 1.3 Rego Output Examples

#### `IN` Operator
```json
{ "field": "department", "comparison": "in", "value": ["Cardiology", "Neurology"] }
```
```rego
allow_rule if {
    "DOCTOR" in input.user.roles
    input.permission == "clinic:visit:create"
    input.resource.department in {"Cardiology", "Neurology"}
}
```

#### `NOT_IN` Operator
```json
{ "field": "paymentMethod", "comparison": "not_in", "value": ["Cash", "Card"] }
```
```rego
deny_rule if {
    "ACCOUNTANT" in input.user.roles
    input.permission == "finance:payment:create"
    not input.resource.paymentMethod in {"Cash", "Card"}
}
```

#### `CONTAINS` Operator
```json
{ "field": "description", "comparison": "contains", "value": "urgent" }
```
```rego
allow_rule if {
    "DOCTOR" in input.user.roles
    input.permission == "clinic:visit:create"
    contains(input.resource.description, "urgent")
}
```

> **Note:** `CONTAINS` is a substring search on string fields. This is different from `IN` which checks set membership. If fields are mostly structured enums/numbers and not free-text, `CONTAINS` may be omitted.

#### `NOT` Group
```json
{
  "operator": "AND",
  "children": [
    { "field": "department", "comparison": "==", "value": "Cardiology" },
    {
      "operator": "NOT",
      "children": [
        { "field": "status", "comparison": "==", "value": "DISCHARGED" }
      ]
    }
  ]
}
```
```rego
allow_rule if {
    "DOCTOR" in input.user.roles
    input.permission == "clinic:visit:create"
    input.resource.department == "Cardiology"
    not _not_block_p1_0
}

_not_block_p1_0 if {
    input.resource.status == "DISCHARGED"
}
```

---

### 1.4 Database Changes

**No database migration needed for Phase 1.** The `expression_json` column already stores free-form JSON. The new operators are just new string values in the `comparison` field, and array values in `value` are already valid JSON.

---

### 1.5 Test Updates

#### [MODIFY] `CompilerTest.java`

Add new test cases:

- `testInOperatorCompilation()` — verifies `in` produces Rego set membership
- `testNotInOperatorCompilation()` — verifies `not_in` produces negated set membership
- `testContainsOperatorCompilation()` — verifies `contains` produces function call
- `testNotGroupSingleCondition()` — verifies `NOT` on a single condition emits helper rule
- `testNotGroupMultipleConditions()` — verifies `NOT` on an AND group emits helper rule with multiple conditions
- `testMixedOperatorsCompilation()` — combines `in`, `not_in`, `NOT`, standard operators in one policy

---

### 1.6 Summary of Phase 1 File Changes

| Layer | File | Change |
|---|---|---|
| AST | `LogicalOperator.java` | Add `NOT` enum value |
| AST | `AstBuilder.java` | Validate `NOT` groups (exactly 1 child) |
| Compiler | `RegoGenerator.java` | Handle `in`, `not_in`, `contains` comparisons + `NOT` groups via helper rules |
| Tests | `CompilerTest.java` | Add 6 new test cases |
| **DB** | **None** | **No migration needed** |

---
---

# Phase 2: Dynamic Custom Rego (Per-Policy)

## Goal

Allow admins to write custom Rego snippets for individual policies when the condition builder is insufficient (e.g., field-to-field comparisons, time-based rules, external data lookups). The toggle is **per-policy** — meaning each row in `authz_policy` independently decides whether to use the condition builder or custom Rego. Auto-generated and custom Rego rules coexist in the same compiled output.

## Flow Change

```
PolicyCompilerService.recompile(namespace)
│
├── Load all enabled policies for namespace
│
├── For EACH policy:
│   │
│   ├── useCustomRego == true?
│   │   ├── YES → append customRegoSnippet directly
│   │   └── NO  → parse expressionJson → AST → DNF → emit rule block
│   │
│   └── (both paths produce Rego rule blocks)
│
├── Wrap all blocks in package header + defaults + final decision rule
│
└── TarGzBundleBuilder → bundle → cache → OPA polls
```

> When `useCustomRego = true`, the policy's `expressionJson` is **preserved but skipped** during compilation. Toggling `useCustomRego` back to `false` restores the original condition builder logic without data loss.

---

## Proposed Changes

### 2.1 Domain Layer

#### [MODIFY] `Policy.java`

Add two new fields:

```java
private final boolean useCustomRego;
private final String customRegoSnippet;
```

Update `create()` and `reconstitute()` factory methods to include these fields (default `useCustomRego = false`, `customRegoSnippet = null`).

Add convenience method:

```java
public boolean hasCustomRego() {
    return useCustomRego && customRegoSnippet != null && !customRegoSnippet.isBlank();
}
```

---

### 2.2 JPA Layer

#### [MODIFY] `PolicyJpaEntity.java`

Add new columns:

```java
@Column(name = "use_custom_rego", nullable = false)
private boolean useCustomRego = false;

@Column(name = "custom_rego_snippet", columnDefinition = "TEXT")
private String customRegoSnippet;
```

#### [MODIFY] `PolicyPersistenceMapper.java`

Update `toDomain()` to map the two new fields from JPA entity to domain model.
Update `updateEntity()` to accept and set the two new fields from domain to JPA entity.

#### [MODIFY] `JpaPolicyRepository.java`

Update the `upsert()` method signature and implementation to pass `useCustomRego` and `customRegoSnippet` through to the mapper.

#### [MODIFY] `PolicyRepository.java` (Port Interface)

Update `upsert()` signature to include `useCustomRego` and `customRegoSnippet` parameters.

---

### 2.3 DTO Layer

#### [MODIFY] `PolicyItemRequest.java`

Add new fields to the record:

```java
public record PolicyItemRequest(
        @NotBlank String permissionCode,
        @NotNull PolicyEffect effect,
        JsonNode expressionJson,
        boolean enabled,
        boolean isDeleted,
        String deletedReason,
        String disabledReason,
        boolean useCustomRego,          // NEW
        String customRegoSnippet        // NEW
) {}
```

#### [MODIFY] `PolicyGridItemDto.java`

Add new fields so the UI knows the current state:

```java
public record PolicyGridItemDto(
        String permissionCode,
        String action,
        String namespace,
        String resourceName,
        Long policyId,
        PolicyEffect effect,
        JsonNode expressionJson,
        boolean enabled,
        String disabledReason,
        String deletedReason,
        boolean deprecated,
        boolean useCustomRego,          // NEW
        String customRegoSnippet        // NEW
) {}
```

> No separate `deprecationWarning` field is needed. The existing `deprecated` boolean handles both auto-generated and custom Rego policies. For custom Rego, deprecation detection is best-effort (string search for `input.resource.<field>`), but the behavior is the same — `deprecated = true` excludes the policy from compilation, and the admin can review and un-flag false positives.

#### [MODIFY] `PolicyDtoMapper.java`

Update `toDto()` to map `useCustomRego` and `customRegoSnippet` from the domain `Policy` to the DTO.

---

### 2.4 Service Layer

#### [MODIFY] `SavePoliciesService.java`

Pass `useCustomRego` and `customRegoSnippet` from `PolicyItemRequest` through to `policyPort.upsert()`:

```java
policyPort.upsert(
        policyId,
        permission.getId(),
        subjectType,
        subjectId,
        item.effect(),
        expressionJson,
        item.enabled(),
        item.disabledReason(),
        item.useCustomRego(),          // NEW
        item.customRegoSnippet()       // NEW
);
```

#### [MODIFY] `PolicyCompilerService.java`

Update `synchronizeDeprecatedPolicies()` to also check custom Rego snippets using the existing `deprecated` field (best-effort string search):

```java
private void synchronizeDeprecatedPolicies() {
    Set<String> deprecatedFields = conditionFieldPort.findAllDeprecated()
            .stream()
            .map(ConditionField::getFieldName)
            .collect(Collectors.toSet());

    List<Policy> activePolicies = policyPort.findAllActive();
    for (Policy policy : activePolicies) {
        boolean usesDeprecatedField;

        if (policy.hasCustomRego()) {
            // Best-effort: string search in custom Rego snippet
            usesDeprecatedField = customRegoMayUseDeprecatedField(
                    policy.getCustomRegoSnippet(), deprecatedFields);
        } else {
            // Existing: structured JSON scan
            String json = policy.getExpressionJson();
            usesDeprecatedField = false;
            if (json != null && !json.trim().isEmpty()) {
                try {
                    JsonNode root = objectMapper.readTree(json);
                    usesDeprecatedField = hasDeprecatedField(root, deprecatedFields);
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        if (policy.isDeprecated() != usesDeprecatedField) {
            policyPort.updateDeprecatedStatus(policy.getId(), usesDeprecatedField);
        }
    }
}

private boolean customRegoMayUseDeprecatedField(
        String regoSnippet, Set<String> deprecatedFields) {
    if (regoSnippet == null) return false;
    for (String field : deprecatedFields) {
        if (regoSnippet.contains("input.resource." + field)) {
            return true;
        }
    }
    return false;
}
```

---

### 2.5 Rego Generator

#### [MODIFY] `RegoGenerator.java`

**Two changes:**

**A. Extract import statements from custom Rego snippets**

Custom Rego snippets may contain `import` statements, but these must appear at the **top** of the file (after `package`), not in the middle where snippets are inserted.

The generator must:
1. Pre-scan all custom Rego snippets for `import ...` lines
2. Strip them from the snippet body
3. Deduplicate and emit all imports at the top of the file

```java
Set<String> collectedImports = new LinkedHashSet<>();
collectedImports.add("import rego.v1");  // always include

// Pre-scan phase
for (Policy policy : policies) {
    if (policy.hasCustomRego()) {
        String cleaned = extractImports(policy.getCustomRegoSnippet(), collectedImports);
        cleanedSnippets.put(policy.getId(), cleaned);
    }
}

// Emit header
sb.append("package app.authz.").append(namespace).append("\n\n");
for (String imp : collectedImports) {
    sb.append(imp).append("\n");
}
sb.append("\n");
```

> **Why?** If an admin writes `import future.keywords.every` inside their snippet, OPA throws a syntax error because imports can't appear mid-file. See [08-custom-rego-edge-cases-and-challenges.md](./08-custom-rego-edge-cases-and-challenges.md#32-import-statements-in-custom-rego-snippets-) for details.

**B. Update the main loop in `generate()`**

```java
for (Policy policy : policies) {
    String permissionCode = permCodeLookup.get(policy.getPermissionId());
    if (permissionCode == null) continue;

    if (policy.hasCustomRego()) {
        // ─── CUSTOM REGO PATH ───
        sb.append("# Policy ID: ").append(policy.getId())
          .append(" (Custom Rego)\n");
        sb.append(cleanedSnippets.get(policy.getId())).append("\n\n");
        continue;
    }

    // ─── EXISTING AUTO-GENERATION PATH (unchanged) ───
    String json = policy.getExpressionJson();
    // ... rest of existing logic ...
}
```

---

### 2.6 Concrete Before/After Example

#### Before: All policies use condition builder

```
Policy 1: ACCOUNTANT + finance:journal:create  │ expressionJson: {amount <= 10000}
Policy 2: MANAGER + finance:payment:approve    │ expressionJson: {department == "Finance"}
Policy 3: AUDITOR + finance:report:export      │ expressionJson: null (unconditional)
```

**Generated Rego:**
```rego
package app.authz.finance

default allow := false
default allow_rule := false
default deny_rule := false

# Policy ID: 1 (Auto-generated)
allow_rule if {
    "ACCOUNTANT" in input.user.roles
    input.permission == "finance:journal:create"
    input.resource.amount <= 10000
}

# Policy ID: 2 (Auto-generated)
allow_rule if {
    "MANAGER" in input.user.roles
    input.permission == "finance:payment:approve"
    input.resource.department == "Finance"
}

# Policy ID: 3 (Unconditional)
allow_rule if {
    "AUDITOR" in input.user.roles
    input.permission == "finance:report:export"
}

allow if {
    allow_rule
    not deny_rule
}
```

#### After: Policy 2 switched to custom Rego

**Generated Rego:**
```rego
package app.authz.finance

default allow := false
default allow_rule := false
default deny_rule := false

# Policy ID: 1 (Auto-generated)
allow_rule if {
    "ACCOUNTANT" in input.user.roles
    input.permission == "finance:journal:create"
    input.resource.amount <= 10000
}

# Policy ID: 2 (Custom Rego)
allow_rule if {
    "MANAGER" in input.user.roles
    input.permission == "finance:payment:approve"
    input.resource.requester_dept == input.user.department
    time.clock(time.now_ns()) >= [9, 0, 0]
    time.clock(time.now_ns()) <= [17, 0, 0]
}

# Policy ID: 3 (Unconditional)
allow_rule if {
    "AUDITOR" in input.user.roles
    input.permission == "finance:report:export"
}

allow if {
    allow_rule
    not deny_rule
}
```

---

### 2.7 Database Migration

#### [NEW] `V6__add_custom_rego_columns_to_policy.sql`

```sql
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
```

---

### 2.8 REST Layer

#### [MODIFY] `PolicyController.java`

No structural changes needed. The existing `PUT /internal/authz/policies` endpoint already accepts `PolicyItemRequest` via `SavePoliciesRequest`. The new fields will be automatically deserialized.

---

### 2.9 Test Updates

#### [MODIFY] `CompilerTest.java`

Add new test cases:

- `testCustomRegoSnippetEmittedDirectly()` — custom Rego is output verbatim
- `testMixedAutoAndCustomRegoPolicies()` — namespace with both types produces correct combined output
- `testCustomRegoToggleOffFallsBackToExpressionJson()` — toggling off restores auto-generated rules
- `testDeprecatedFieldDetectionInCustomRego()` — best-effort string search catches deprecated fields

---

### 2.10 Save-Time Validations for Custom Rego

These validations must be added to `SavePoliciesService` when `useCustomRego = true`:

```java
private void validateCustomRego(PolicyItemRequest item, String expectedPermCode) {
    String snippet = item.customRegoSnippet();
    
    // 1. Not empty
    if (snippet == null || snippet.isBlank()) {
        throw new InvalidPayloadException("Custom Rego snippet cannot be empty.");
    }

    // 2. Max length (prevent bundle size explosion)
    if (snippet.length() > 10_000) {
        throw new InvalidPayloadException("Custom Rego snippet exceeds maximum length.");
    }

    // 3. Only allow_rule/deny_rule blocks (no allow :=, no package, no direct imports)
    validateSnippetRuleNames(snippet);

    // 4. Permission code matches policy's actual permission
    validatePermissionInSnippet(snippet, expectedPermCode);

    // 5. Rego syntax check (via opa CLI — OUTSIDE the @Transactional block)
    regoValidator.validateSyntax(snippet);
}
```

> **Critical:** Rego syntax validation (step 5) MUST happen **before** the `@Transactional` block to avoid holding DB connections during I/O. See [08-custom-rego-edge-cases-and-challenges.md](./08-custom-rego-edge-cases-and-challenges.md) for all edge cases.

---

### 2.11 Rego Validation Before Bundle Cache Write

The `recompile()` method must validate the generated Rego **before** overwriting the bundle cache. Without this, a bad custom Rego snippet corrupts the cache and causes a total namespace outage on OPA restart.

```java
public synchronized void recompile(String targetNamespace) {
    synchronizeDeprecatedPolicies();
    
    String regoContent = generator.generate(policies, permissions);

    // Validate BEFORE overwriting the cache
    boolean isValid = regoValidator.validateSyntax(regoContent);
    if (!isValid) {
        throw new RegoCompilationException(
            "Generated Rego for namespace '" + targetNamespace 
            + "' has syntax errors. Bundle NOT updated.");
    }

    // Compute ETag from Rego text content (not compressed bytes)
    String contentHash = md5(regoContent);
    String currentEtag = bundleCachePort.getEtag(targetNamespace);
    
    // Only update bundle if content actually changed (avoids ETag thrashing)
    if (!contentHash.equals(currentEtag)) {
        byte[] bundleBytes = bundleService.build(targetNamespace, regoContent);
        bundleCachePort.upsertBundle(targetNamespace, bundleBytes, contentHash);
    }
}
```

> **ETag fix:** The current `TarGzBundleService` includes `System.currentTimeMillis()` in the tar header, causing different bytes on every build. Computing ETag from the Rego text (before compression) ensures OPA only re-downloads when content actually changes.

---

### 2.12 Enforcement Layer Changes for Custom Rego

Custom Rego can reference fields not available in the current OPA input payload. Two enforcement layer changes are required.

#### [MODIFY] `EvaluationPayload.java`

Extend `User` with an extensible attributes map:

```java
@Data
@Builder
public static class User {
    private String id;
    private List<String> roles;
    private Map<String, Object> attributes;   // NEW — extensible user context
}
```

#### [MODIFY] `SpringSecurityPolicyEnforcer.java`

Populate `attributes` from JWT claims:

```java
Map<String, Object> userAttributes = new HashMap<>();
if (json.has("department")) userAttributes.put("department", json.get("department").asText());
if (json.has("tenant_id")) userAttributes.put("tenant_id", json.get("tenant_id").asText());
// Or: extract all non-standard JWT claims

EvaluationPayload.User.builder()
    .id(userId)
    .roles(roles)
    .attributes(userAttributes)
    .build();
```

Custom Rego references user attributes via: `input.user.attributes.department`

> **Why?** The current `EvaluationPayload.User` only has `id` and `roles`. Custom Rego referencing `input.user.department` (field-to-field comparison — a primary use case for custom Rego) silently fails because the field is never in the OPA input. See [08-custom-rego-edge-cases-and-challenges.md](./08-custom-rego-edge-cases-and-challenges.md#41-inputuser-is-hardcoded--only-id-and-roles-) for details.

#### [MODIFY] `PolicyField.java` annotation

Add `hidden` attribute for fields that should be sent to OPA but not shown in the condition builder UI:

```java
@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PolicyField {
    String displayName();
    FieldType type();
    String[] allowedValues() default {};
    String optionsEndpoint() default "";
    boolean hidden() default false;   // NEW — sent to OPA but hidden from condition builder UI
}
```

---

### 2.13 Edge Cases & Safeguards Summary

| Edge Case | How It's Handled |
|---|---|
| `useCustomRego = true` but `customRegoSnippet` is null/blank | DB constraint `chk_custom_rego_consistency` prevents this. Backend validation + `Policy.hasCustomRego()` double-checks. |
| User toggles `useCustomRego` off | `expressionJson` was never deleted — auto-generated compilation resumes immediately. UI warning recommended. |
| Deprecated field in custom Rego | Best-effort string search (comment-stripped, word-boundary matched) → sets `deprecated = true`. Admin can un-flag false positives. |
| Custom Rego has syntax errors | Validated via `opa` CLI both at save-time AND before bundle cache write. Bad Rego never reaches the cache. |
| Wrong permission code in snippet | Regex validation on save extracts `input.permission == "..."` and compares to policy's actual `permissionCode`. |
| Import statements in snippet | Extracted, deduplicated, and hoisted to file header by `RegoGenerator`. |
| Custom Rego references unreachable fields | `input.user.attributes` map added for user context. `@PolicyField(hidden=true)` added for resource fields. |
| Multiple policies in same namespace: some custom, some auto | Fully supported — `RegoGenerator` iterates all policies and uses the appropriate path per-policy. |
| `customRegoSnippet` includes package header or defaults | Admin should NOT include these — only rule blocks. Semantic validation rejects non-`allow_rule`/`deny_rule` top-level rules. |

---

## Complete File Change Summary

### Phase 1 — Enhanced Condition Builder (No DB Migration)

| Layer | File | Change |
|---|---|---|
| AST | `LogicalOperator.java` | Add `NOT` enum value |
| AST | `AstBuilder.java` | Validate `NOT` groups (exactly 1 child) |
| Compiler | `RegoGenerator.java` | Handle `in`, `not_in`, `contains`, `NOT` helper rules |
| Tests | `CompilerTest.java` | Add 6 new test cases |

### Phase 2 — Dynamic Custom Rego (Flyway Migration Required)

| Layer | File | Change |
|---|---|---|
| Domain | `Policy.java` | Add `useCustomRego`, `customRegoSnippet`, `hasCustomRego()` |
| DTO | `PolicyItemRequest.java` | Add 2 new fields |
| DTO | `PolicyGridItemDto.java` | Add 2 new fields |
| DTO | `PolicyDtoMapper.java` | Map new fields |
| DTO | `EvaluationPayload.java` | Add `attributes` map to `User` |
| Port | `PolicyRepository.java` | Update `upsert()` signature |
| Service | `SavePoliciesService.java` | Pass new fields + custom Rego validations |
| Service | `PolicyCompilerService.java` | Deprecation check + Rego validation before cache write + content-based ETag |
| Compiler | `RegoGenerator.java` | Custom Rego branch + import extraction/hoisting |
| Annotation | `PolicyField.java` | Add `hidden` attribute |
| Enforcer | `SpringSecurityPolicyEnforcer.java` | Populate `user.attributes` from JWT |
| JPA | `PolicyJpaEntity.java` | Add 2 new columns |
| JPA | `PolicyPersistenceMapper.java` | Map new fields |
| JPA | `JpaPolicyRepository.java` | Pass new fields in `upsert()` |
| DB | `V6__add_custom_rego_columns_to_policy.sql` | NEW migration |
| Tests | `CompilerTest.java` | Add 4 new test cases |

---

## Verification Plan

### Automated Tests
```bash
mvn test -pl authz-core/authz-core -Dtest=CompilerTest
mvn test
```

### Manual Verification
1. Start the backend application
2. **Phase 1**: Save a policy with `"comparison": "in"` and `"value": ["A", "B"]` → verify Rego uses set membership
3. **Phase 1**: Save a policy with a `NOT` group → verify helper rule is generated
4. **Phase 2**: Save a policy with `"useCustomRego": true` → verify Rego includes snippet verbatim
5. **Phase 2**: Toggle `useCustomRego` back to `false` → verify auto-generated rules resume
6. **Phase 2**: Deprecate a field → verify `deprecated = true` on custom Rego policies referencing it
