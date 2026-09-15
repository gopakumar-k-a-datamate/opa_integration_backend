# OPA Endpoints Pagination and Search Guide

This guide documents the Bedrock-compliant pagination and search functionality implemented for OPA authorization management endpoints (`/internal/authz/policies` and `/internal/authz/subjects`). It provides backend integration details, frontend usage standards, and API contracts for developers.

---

## 1. Overview & Architectural Standards

To align with Bedrock framework standards (`com.datamate.bedrock.framework.common.pagination`), authorization management APIs support 1-indexed pagination and text search filtering.

### Key Principles:
* **Bedrock Paged Response Format:** Paginated endpoints return `Paged<T>` objects containing `content` (or `items`), `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `hasNext`, and `hasPrevious`.
* **1-Based Page Indexing:** `page=1` refers to the first page.
* **Backward Compatibility:** `GET /internal/authz/policies` returns a raw `List<PolicyGridItemDto>` array when `page` and `size` parameters are omitted, and returns a `Paged<PolicyGridItemDto>` object when `page` or `size` is provided.
* **PostgreSQL Safe Querying:** Search parameter queries in Spring Data JPA explicitly cast string parameters to avoid PostgreSQL null parameter binding type errors (`ERROR: function lower(bytea) does not exist`).

---

## 2. API Endpoint Contracts

### A. Policies Endpoint (`GET /internal/authz/policies`)

Retrieves permissions registered in the system along with any database policy bindings for a specific subject and namespace.

#### Query Parameters:
| Parameter | Type | Required | Default | Description |
| :--- | :--- | :---: | :---: | :--- |
| `subjectType` | `String` (`ROLE` \| `USER`) | **Yes** | — | Target subject classification |
| `subjectId` | `String` | **Yes** | — | Identifier of the subject (e.g., `ACCOUNTANT`, `PHARMACIST`) |
| `namespace` | `String` | **Yes** | — | Target namespace (e.g., `pharmacy`) |
| `search` | `String` | No | `null` | Substring search across `permissionCode`, `resourceName`, and `action` |
| `page` | `Integer` | No | `null` | Page number (1-indexed). Triggers `Paged<T>` response when supplied. |
| `size` | `Integer` | No | `null` | Number of items per page. Defaults to `10` when `page` is provided. |

#### Searched Fields:
* `permissionCode` (e.g., `"pharmacy:prescription:create"`)
* `resourceName` (e.g., `"prescription"`)
* `action` (e.g., `"create"`, `"dispense"`)

---

### B. Subjects Endpoint (`GET /internal/authz/subjects`)

Retrieves synced subjects (roles and users) for policy assignment pickers and dropdowns.

#### Query Parameters:
| Parameter | Type | Required | Default | Description |
| :--- | :--- | :---: | :---: | :--- |
| `type` / `subjectType` | `String` (`ROLE` \| `USER`) | No | `null` | Filter by subject type |
| `search` | `String` | No | `null` | Substring search across `subjectId`, `displayName`, and `subjectName` |
| `page` | `Integer` | No | `1` | Page number (1-indexed) |
| `size` | `Integer` | No | `10` | Items per page |

#### Searched Fields:
* `subjectId` (e.g., `"PHARMACIST"`)
* `displayName` (e.g., `"Lead Pharmacist"`)
* `subjectName` (e.g., `"pharmacist"`)

---

## 3. Response Contracts

### Paginated Response Payload (`Paged<T>`)

```json
{
  "content": [
    {
      "permissionCode": "pharmacy:prescription:create",
      "action": "create",
      "namespace": "pharmacy",
      "resourceName": "prescription",
      "policyId": 12,
      "effect": "ALLOW",
      "expressionJson": "{...}",
      "enabled": true,
      "disabledReason": null,
      "deletedReason": null,
      "deprecated": false,
      "useCustomRego": false,
      "customRegoSnippet": null
    }
  ],
  "pageNumber": 1,
  "pageSize": 5,
  "totalElements": 2,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

---

## 4. Frontend Integration Guide

### Example 1: Fetching Paginated & Filtered Policies (JavaScript / React)

```javascript
async function fetchPolicies({ subjectType, subjectId, namespace, search = '', page = 1, size = 10 }) {
  const params = new URLSearchParams({
    subjectType,
    subjectId,
    namespace,
    page: page.toString(),
    size: size.toString()
  });

  if (search && search.trim() !== '') {
    params.append('search', search.trim());
  }

  const response = await fetch(`/internal/authz/policies?${params.toString()}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch policies: ${response.statusText}`);
  }

  const data = await response.json();
  // data contains: { content: [...], pageNumber: 1, pageSize: 10, totalElements: 25, totalPages: 3 }
  return data;
}
```

### Example 2: Subject Picker Search (React Select / Autocomplete)

```javascript
async function searchSubjects(searchTerm, subjectType = 'ROLE', page = 1, size = 10) {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString()
  });

  if (subjectType) params.append('type', subjectType);
  if (searchTerm) params.append('search', searchTerm.trim());

  const res = await fetch(`/internal/authz/subjects?${params.toString()}`);
  const pagedResult = await res.json();
  
  return pagedResult.content.map(subject => ({
    value: subject.subjectId,
    label: `${subject.displayName} (${subject.subjectId})`
  }));
}
```
