# Fieldwise & Multi-Field Comparisons — Manual Testing Guide

This guide provides step-by-step instructions for manually testing and verifying **Fieldwise (`FIELD`)** and **Multi-Field List Inclusion (`FIELD_LIST`)** policies alongside traditional **Value (`VALUE`)** policies in the `pharmacy-microservice`.

---

## 1. Identity Service Integration & Subject Role Setup

In our microservice architecture, **Identity Service** is the single source of truth for Users and Roles. Microservices project these subjects into their local `authz_subject` table automatically via RabbitMQ event synchronization (`auth.subject.sync`).

### Step 1.1: Create & Activate Role in Identity Service via Swagger UI
1. Open **Swagger UI** for Identity Service (`http://localhost:8085/swagger-ui.html`).
2. Execute the role creation endpoint to create a new Role (e.g., Name: `PHARMACIST` or `PHARMACY`).
3. Set the role status to **Active** (or call the role activation endpoint).
4. **Copy the Role UUID** returned by Identity Service (e.g., `17f81b21-6824-4ac0-8429-310351f26bd0`).

> ℹ️ **How it works**: Activating the role in Identity Service automatically publishes a `ROLE_CREATED` event onto the RabbitMQ `auth.subject.sync` exchange. `pharmacy-microservice` listens to this event and projects the role into its local `authz_subject` table.

---

## 2. Boot Environment

### Step 2.1: Start Infrastructure & Microservices
Ensure RabbitMQ, Identity Service, OPA sidecar, and `pharmacy-microservice` are running:

```bash
cd c:\Users\krish\MyProjects\DataMate\opa_integration_backend\pharmacy-microservice
docker-compose up -d
mvn spring-boot:run
```

Wait until application logs indicate:
`Started PharmacyApplication in ... seconds (Server port: 8083)`

---

## 3. Phase 1: Policy Creation via Admin API

We register authorization policies for your newly created Role UUID (`<YOUR_ROLE_UUID>`) in the `pharmacy` namespace via `PUT http://localhost:8083/internal/authz/policies`.

### 3.1 Policy Payload
Execute the following `cURL` request, replacing `<YOUR_ROLE_UUID>` with the UUID copied from Swagger UI:

```bash
curl -X PUT http://localhost:8083/internal/authz/policies \
  -H "Content-Type: application/json" \
  -d '{
    "subjectType": "ROLE",
    "subjectId": "<YOUR_ROLE_UUID>",
    "namespace": "pharmacy",
    "policies": [
      {
        "permissionCode": "pharmacy:medication:dispense",
        "effect": "ALLOW",
        "enabled": true,
        "expressionJson": {
          "operator": "AND",
          "children": [
            {
              "field": "quantity",
              "comparison": "<=",
              "value": 100,
              "valueType": "VALUE"
            },
            {
              "field": "assignedLocation",
              "comparison": "==",
              "value": "user.location",
              "valueType": "FIELD"
            },
            {
              "field": "user.department",
              "comparison": "in",
              "value": "resource.allowedDepartments",
              "valueType": "FIELD_LIST"
            }
          ]
        }
      }
    ]
  }'
```

---

## 4. Phase 2: Direct OPA Engine Verification

OPA polls `http://localhost:8083/internal/authz/bundle/pharmacy` to sync compiled Rego rules.

---

###  Test Evaluation directly on OPA (`localhost:8183`)

#### Test Case A: SUCCESS (All conditions met)
- **User**: role `<YOUR_ROLE_UUID>`, location `NORTH_WING`, department `ICU`
- **Resource**: quantity `50` (`<= 100`), assignedLocation `NORTH_WING` (`== user.location`), allowedDepartments `["ICU", "EMERGENCY"]`

```bash
curl -X POST http://localhost:8183/v1/data/app/authz/pharmacy/allow \
  -H "Content-Type: application/json" \
  -d '{
    "input": {
      "permission": "pharmacy:medication:dispense",
      "user": {
        "roles": ["<YOUR_ROLE_UUID>"],
        "location": "NORTH_WING",
        "department": "ICU"
      },
      "resource": {
        "quantity": 50,
        "assignedLocation": "NORTH_WING",
        "allowedDepartments": ["ICU", "EMERGENCY"]
      }
    }
  }'
```
**Expected Response**: `{"result": true}`

---

#### Test Case B: FAILURE — Fieldwise Location Mismatch (`valueType: "FIELD"`)
- **User**: location `SOUTH_WING`
- **Resource**: assignedLocation `NORTH_WING`

```bash
curl -X POST http://localhost:8183/v1/data/app/authz/pharmacy/allow \
  -H "Content-Type: application/json" \
  -d '{
    "input": {
      "permission": "pharmacy:medication:dispense",
      "user": {
        "roles": ["<YOUR_ROLE_UUID>"],
        "location": "SOUTH_WING",
        "department": "ICU"
      },
      "resource": {
        "quantity": 50,
        "assignedLocation": "NORTH_WING",
        "allowedDepartments": ["ICU", "EMERGENCY"]
      }
    }
  }'
```
**Expected Response**: `{"result": false}`

---

#### Test Case C: FAILURE — Department Not In List (`valueType: "FIELD_LIST"`)
- **User**: department `PEDIATRICS` (not in `["ICU", "EMERGENCY"]`)

```bash
curl -X POST http://localhost:8183/v1/data/app/authz/pharmacy/allow \
  -H "Content-Type: application/json" \
  -d '{
    "input": {
      "permission": "pharmacy:medication:dispense",
      "user": {
        "roles": ["<YOUR_ROLE_UUID>"],
        "location": "NORTH_WING",
        "department": "PEDIATRICS"
      },
      "resource": {
        "quantity": 50,
        "assignedLocation": "NORTH_WING",
        "allowedDepartments": ["ICU", "EMERGENCY"]
      }
    }
  }'
```
**Expected Response**: `{"result": false}`
