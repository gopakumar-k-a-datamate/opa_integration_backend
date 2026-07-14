# Dynamic Policy-Based Authorization using OPA

## Implementation Plan

---

# 1. Problem Statement

The current authorization model only supports mapping **Roles → Permissions**.

Example:

```text
ACCOUNTANT
    ↓
journal:create
```

This approach becomes insufficient when authorization depends on business rules.

For example:

* An Accountant can create journals only up to **₹10,000**.
* A Manager can create journals up to **₹20,000**.
* A specific user should be denied regardless of their role.
* Administrators should be able to create nested conditions such as:

```text
(amount <= 10000 AND bank != CASH)
OR
(user.department == FINANCE)
```

The system should allow administrators to:

* Create permissions dynamically.
* Assign permissions to roles (and later other subjects).
* Define complex authorization conditions.
* Create arbitrary nested AND/OR expressions.
* Optionally write custom Rego snippets.
* Automatically generate OPA Rego bundles whenever policies change.

The goal is to keep **OPA as the Policy Decision Point (PDP)** while allowing all policy management through the application's UI without manually writing Rego.

---

# 2. High-Level Architecture

```text
Administrator UI
        │
        ▼
Policy Management API (Java)
        │
        ▼
Database
        │
        ▼
Policy Compiler
        │
        ▼
Generated Rego Files
        │
        ▼
bundle.tar.gz
        │
        ▼
OPA Sidecar
        │
        ▼
Application Authorization Request
```

---

# 3. Policy Model

Instead of thinking in terms of **Role → Permission**, the system should manage **Policies**.

A policy answers:

* Who is this policy for?
* Which permission does it govern?
* Under what conditions is access allowed or denied?

Conceptually:

```text
Namespace
      │
Resource (Bounded Context)
      │
Permission
      │
Policy
      ├── Subject
      ├── Effect
      ├── Expression
      ├── Custom Rego
      └── Enabled
```

---

# 4. Database Design

## 4.1 permission_namespace

Represents a business domain.

| Column      | Type      |
| ----------- | --------- |
| id          | UUID      |
| name        | VARCHAR   |
| description | VARCHAR   |
| created_at  | TIMESTAMP |
| updated_at  | TIMESTAMP |

Example

| name      |
| --------- |
| finance   |
| clinical  |
| inventory |

---

## 4.2 resource

Represents a bounded context or protected resource.

| Column       | Type    |
| ------------ | ------- |
| id           | UUID    |
| namespace_id | FK      |
| name         | VARCHAR |
| description  | VARCHAR |

Example

| Namespace | Resource     |
| --------- | ------------ |
| finance   | journal      |
| finance   | bank_account |
| clinical  | patient      |

---

## 4.3 permission

Represents an action.

| Column      | Type    |
| ----------- | ------- |
| id          | UUID    |
| resource_id | FK      |
| action      | VARCHAR |
| code        | VARCHAR |
| description | VARCHAR |

Generated permission codes

```text
finance:journal:create

finance:journal:update

clinical:patient:view
```

---

## 4.4 role

| Column      | Type    |
| ----------- | ------- |
| id          | UUID    |
| name        | VARCHAR |
| description | VARCHAR |

---

## 4.5 policy

Represents an authorization rule.

| Column          | Type              |
| --------------- | ----------------- |
| id              | UUID              |
| permission_id   | FK                |
| subject_type    | ENUM              |
| subject_id      | UUID/String       |
| effect          | ENUM(ALLOW, DENY) |
| enabled         | BOOLEAN           |
| expression_json | JSON              |
| custom_rego     | TEXT              |
| created_at      | TIMESTAMP         |
| updated_at      | TIMESTAMP         |

Supported subject types:

```text
ROLE
USER
```

Future extension:

```text
GROUP

DEPARTMENT

TENANT
```

---

# 5. Why store JSON instead of normalized condition tables?

The UI supports:

* Nested AND groups
* Nested OR groups
* Arbitrary grouping
* Unlimited nesting

Example

```text
((A AND B)
 OR
(C AND D))
AND
(E OR F)
```

A relational table cannot naturally preserve this hierarchy without introducing recursive parent-child structures.

Instead, store the expression tree directly as JSON.

Example

```json
{
  "operator":"AND",
  "children":[
    {
      "field":"resource.amount",
      "comparison":"<=",
      "value":10000
    },
    {
      "operator":"OR",
      "children":[
        {
          "field":"resource.bank",
          "comparison":"!=",
          "value":"CASH"
        },
        {
          "field":"user.department",
          "comparison":"==",
          "value":"FINANCE"
        }
      ]
    }
  ]
}
```

Advantages

* Preserves the complete expression hierarchy.
* Eliminates complex recursive database joins.
* Matches the structure produced by most visual rule builders.
* Can be recursively compiled into Rego.

---

# 6. Policy Evaluation Model

A user may have multiple roles.

Example

```text
John

Roles

ACCOUNTANT

MANAGER
```

Policies

```text
ACCOUNTANT

journal:create

amount <= 10000
```

```text
MANAGER

journal:create

amount <= 20000
```

John creates a journal for ₹15,000.

Evaluation

```text
ACCOUNTANT

false
```

```text
MANAGER

true
```

Final Result

```text
ALLOW
```

The authorization model follows a **union-of-roles** approach:

* Every assigned role contributes additional permissions.
* If any applicable **ALLOW** policy evaluates to `true`, access is granted.
* If **DENY** policies are introduced, they should override matching ALLOW policies.

This naturally supports users with multiple roles.

---

# 7. Policy Compiler

The compiler converts database records into Rego.

Pipeline

```text
Database
      │
      ▼
Load Policies
      │
      ▼
Deserialize Expression JSON
      │
      ▼
Expression Tree
      │
      ▼
Compile to Rego
      │
      ▼
Generate Bundle
      │
      ▼
bundle.tar.gz
```

---

# 8. Bundle Generation Workflow

## Step 1

OPA requests

```http
GET /bundles/authz
```

---

## Step 2

Load all enabled policies.

```sql
SELECT *
FROM policy
WHERE enabled = true;
```

---

## Step 3

Group policies by namespace.

Example

```text
finance

clinical

inventory
```

---

## Step 4

Parse `expression_json`.

The JSON is converted into an in-memory expression tree.

---

## Step 5

Compile the expression tree recursively.

Example

Expression

```text
amount <=10000

AND

bank != CASH
```

Generated Rego

```rego
input.resource.amount <= 10000

input.resource.bank != "CASH"
```

Nested groups are compiled recursively until the entire expression becomes valid Rego.

---

## Step 6

Generate one Rego rule per policy.

Example

```rego
allow if {

    "ACCOUNTANT" in input.user.roles

    input.permission == "finance:journal:create"

    input.resource.amount <= 10000
}
```

Manager

```rego
allow if {

    "MANAGER" in input.user.roles

    input.permission == "finance:journal:create"

    input.resource.amount <= 20000
}
```

User-specific deny

```rego
deny if {

    input.user.id == 10
}
```

---

## Step 7

Append custom Rego.

If administrators provide additional Rego snippets, append them to the generated rule during compilation.

---

## Step 8

Generate namespace-specific files.

Instead of generating one file per policy:

```text
finance.rego

clinical.rego

inventory.rego
```

Each file contains all policies for that namespace.

---

## Step 9

Create the bundle.

```text
bundle/

    finance.rego

    clinical.rego

    inventory.rego

    manifest.json
```

Compress into

```text
bundle.tar.gz
```

Return the bundle as the HTTP response to the OPA sidecar.

---

# 9. Suggested Java Components

```text
BundleController
        │
        ▼
BundleGenerationService
        │
        ▼
PolicyRepository
        │
        ▼
ExpressionParser
        │
        ▼
RegoCompiler
        │
        ▼
BundleWriter
```

### Responsibilities

| Component                   | Responsibility                                                                                                             |
| --------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| **BundleController**        | Exposes the bundle endpoint for OPA.                                                                                       |
| **BundleGenerationService** | Coordinates bundle generation.                                                                                             |
| **PolicyRepository**        | Loads enabled policies from the database.                                                                                  |
| **ExpressionParser**        | Converts `expression_json` into an in-memory expression tree.                                                              |
| **RegoCompiler**            | Recursively traverses the expression tree and generates Rego.                                                              |
| **BundleWriter**            | Groups rules by namespace, generates `.rego` files, creates `manifest.json`, and packages everything into `bundle.tar.gz`. |

---

# 10. Future Enhancements

The proposed design is extensible and supports future requirements with minimal schema changes:

* Additional subject types (Group, Department, Tenant)
* Time-based policies
* IP or network-based restrictions
* Resource ownership checks
* Additional comparison operators (`IN`, `BETWEEN`, `MATCHES`, etc.)
* Policy versioning and auditing
* Bundle caching and incremental regeneration
* Policy testing and simulation before deployment

---

# Final Outcome

This design separates **policy management** from **policy execution**:

* Administrators define policies through the application's UI.
* Policies are stored as structured metadata and expression trees, not raw Rego.
* A dedicated compiler transforms these policies into optimized Rego files.
* The backend packages the generated policies into an OPA bundle (`tar.gz`).
* The OPA sidecar downloads the bundle and evaluates authorization requests at runtime.

This approach keeps business rules editable without modifying application code while leveraging OPA as the runtime policy engine. It also provides a scalable foundation for evolving from simple RBAC toward richer attribute-based and policy-based authorization as new business requirements emerge.
