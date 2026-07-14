# REST API Endpoints (Federated Library)

This document outlines the REST APIs exposed by the `authz-core` library. When a microservice (or modulith) includes this library, these endpoints are automatically provisioned to serve the Admin UI and the local OPA sidecar.

All library endpoints are typically prefixed with `/internal/authz/` and should be secured via API Gateway or internal network routing.

---

## 1. Admin UI: Role-Permission Grid

### GET `/internal/authz/policies`
Fetches the complete permission matrix for a specific subject (Role or User) within this specific application module.

**Query Parameters:**
- `subjectType` (required): `ROLE` or `USER`
- `subjectId` (required): e.g., `ACCOUNTANT` or `123`

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
  "policies": [
    {
      "permissionCode": "finance:journal:create",
      "effect": "ALLOW",
      "expressionJson": { ... },
      "enabled": true,
      "isDeleted": false
    },
    {
      "permissionCode": "finance:journal:delete",
      "isDeleted": true
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
    "optionsEndpoint": "/api/finance/banks"
  }
]
```

---

## 3. OPA Runtime

### GET `/internal/authz/bundle`
Serves the compiled OPA bundle (`bundle.tar.gz`) for this application module. Queried continuously by the local OPA sidecar.

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
