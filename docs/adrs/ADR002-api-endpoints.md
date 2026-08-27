# REST API Endpoints (Federated Library)

This document outlines the REST APIs that **must be exposed** by each microservice (or modulith) integrating with the `authz-core` framework. 

In the Bedrock architecture, the `authz-core` library provides the core domain logic and services (e.g., `PolicyManagement`), but the microservice itself acts as the "Driving Adapter", exposing these services via its own `@RestController` or Spring Router functions.

All of these policy-management endpoints are typically prefixed with `/internal/authz/` and should be secured via API Gateway or internal network routing.

---

## 1. Admin UI: Role-Permission Grid

### GET `/internal/authz/policies`
Fetches the complete permission matrix for a specific subject (Role or User) within this specific application module.

**Query Parameters:**
- `subjectType` (required): `ROLE` or `USER`
- `subjectId` (required): e.g., `ACCOUNTANT` or `123`
- `namespace` (required): The module namespace to fetch policies for (e.g., `finance`)

**Response (`200 OK`):**
```json
[
  {
    "permissionCode": "finance:journal:create",
    "action": "create",
    "namespace": "finance",
    "resourceName": "journal",
    "policyId": 105,
    "effect": "ALLOW",
    "expressionJson": {
      "operator": "AND",
      "children": [
        { "field": "amount", "comparison": "<=", "value": 10000 }
      ]
    },
    "enabled": true,
    "disabledReason": null
  },
  {
    "permissionCode": "finance:journal:delete",
    "action": "delete",
    "namespace": "finance",
    "resourceName": "journal",
    "policyId": null,
    "effect": null,
    "expressionJson": null,
    "enabled": false,
    "disabledReason": null
  }
]
```

### PUT `/internal/authz/policies`
Performs a **Full-State Sync** for the specified Subject in this module. Because the UI manages the entire matrix for a given Role within a specific Module, the payload represents the **Desired State**. 

**Sync Logic:**
1. Fetch all existing policies for this subject in the local DB.
2. Iterate payload: Update existing matches, insert new ones.
3. Soft-delete any existing policies in the DB that are *missing* from the incoming payload (meaning the Admin unchecked them).
4. Trigger the Policy Compiler to regenerate the OPA bundle.

**Request Body:**
```json
{
  "subjectType": "ROLE",
  "subjectId": "ACCOUNTANT",
  "namespace": "finance",
  "policies": [
    {
      "permissionCode": "finance:journal:create",
      "effect": "ALLOW",
      "expressionJson": { ... },
      "enabled": true,
      "disabledReason": null,
      "isDeleted": false,
      "deletedReason": null
    },
    {
      "permissionCode": "finance:journal:delete",
      "isDeleted": true,
      "deletedReason": "Policy revoked by admin"
    }
  ]
}
```

**Response (`200 OK`):**
```json
{
  "message": "Policies updated successfully. OPA bundle regenerated."
}
```

---

### GET `/internal/authz/namespaces`
Fetches all unique namespaces registered in this microservice. Used by the UI to dynamically discover which module tabs should be rendered.

**Response (`200 OK`):**
```json
[
  "finance",
  "payroll"
]
```

---

## 2. Admin UI: Condition Builder

### GET `/internal/authz/permissions/{permissionCode}/fields`
Fetches the active `authz_condition_field` definitions for a specific permission. Used by the Condition Builder UI to populate the field dropdowns.

**Path Variables:**
- `permissionCode` (required): e.g., `finance:journal:create`

**Response (`200 OK`):**
```json
[
  {
    "fieldName": "amount",
    "fieldType": "NUMBER",
    "displayName": "Journal Amount",
    "allowedValues": null,
    "optionsEndpoint": null
  },
  {
    "fieldName": "bank",
    "fieldType": "STRING",
    "displayName": "Bank Account",
    "allowedValues": null,
    "optionsEndpoint": "/internal/authz/options/banks"
  }
]
```

### 2.1 Dynamic Options Endpoints (Convention)

When a condition field defines an `optionsEndpoint`, the Admin UI Condition Builder will dynamically fetch its dropdown values from that URL instead of using a static `allowedValues` list. 

**Priority Rule:**
If a field has both `allowedValues` (static) and an `optionsEndpoint` (dynamic) defined in the database, **the `optionsEndpoint` always wins**. The UI assumes the endpoint provides the most up-to-date live data, and the static list is ignored.

**Critical Design Convention:**
All `optionsEndpoint` paths **MUST** be defined as internal routes (e.g., `/internal/authz/options/banks`), not public application routes (e.g., `/api/finance/banks`). 

**Why?**
1. **CORS:** The Admin UI already has Cross-Origin Resource Sharing (CORS) access to the `/internal/authz/**` paths on every microservice. Using this namespace avoids having to configure CORS on public business APIs just for the Admin UI.
2. **Authentication:** The Admin UI sits within the internal secure network zone (API Gateway). The `/internal/authz/**` paths are protected at the network level and do not require passing JWT bearer tokens. Public business APIs (`/api/v1/...`) would reject the Admin UI's bare `fetch()` requests with a `401 Unauthorized`.

**Response Format:**
The UI expects dynamic option endpoints to return a JSON array of objects containing an `id` (the underlying value saved in the policy) and a `display` (the human-readable label shown in the dropdown):
```json
[
  { "id": "HDFC", "display": "HDFC Bank" },
  { "id": "SBI", "display": "State Bank of India" }
]
```

---

## 3. OPA Runtime

### GET `/internal/authz/bundle/{namespace}`
Serves the compiled OPA bundle (`bundle.tar.gz`) for a specific namespace in this application module. Queried continuously by the local OPA sidecar.

**Path Variables:**
- `namespace` (required): e.g., `finance`, `clinical`

**Headers:**
- `If-None-Match`: The ETag (MD5 hash) of the bundle OPA currently holds.

**Response (`200 OK`):**
Returns the binary `bundle.tar.gz` payload.
- `ETag`: `"d41d8cd98f00b204e9800998ecf8427e"`
- `Content-Type`: `application/gzip`

**Response (`304 Not Modified`):**
Returned if the client's `If-None-Match` header matches the latest bundle ETag in the database, meaning no policies have changed. This saves massive amounts of bandwidth during OPA polling.

---

## 4. Identity Provider (IdP) Dependencies
*(Note: These endpoints live on the Central Identity Module, NOT the `authz-core` library).*

### GET `/api/idp/roles`
Fetches the global list of roles. The Admin UI calls this first to populate the Role dropdown selector before querying the individual module libraries for policies.

**Response (`200 OK`):**
```json
[
  { "id": 1, "name": "ACCOUNTANT" },
  { "id": 2, "name": "MANAGER" },
  { "id": 3, "name": "ADMIN" }
]
```
