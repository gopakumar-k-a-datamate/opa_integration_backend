# 07 - Authz-Core Integration Testing Guide

## 1. Introduction

This guide provides comprehensive instructions for validating the `authz-core` library's capabilities. Specifically, it details how to test the automated Rego bundle generation and Open Policy Agent (OPA) integration within microservice and modular monolith architectures. 

The primary objective is to verify that complex, nested authorization conditions (`AND`/`OR` trees) created by administrators via the UI are accurately compiled into Rego logic and enforced by the OPA sidecar at runtime.

---

## 2. Test Environments Overview

Two distinct testing environments have been scaffolded to simulate real-world usage patterns of the `authz-core` library.

### 2.1. Finance Microservice (`finance-microservice`)
*   **Architecture Strategy:** Simulates an isolated, single-domain microservice.
*   **Target Namespace:** `finance`
*   **Monitored Resource:** `Journal` (Actions: Create, Read, Approve)
*   **OPA Configuration:** Single-bundle polling from the microservice.
*   **Service Port:** `8081` | **OPA Port:** `8181`

### 2.2. Clinic Modulith (`clinic-modulith`)
*   **Architecture Strategy:** Simulates a Modular Monolith maintaining strict internal namespace boundaries.
*   **Target Namespaces:** `clinical`, `billing`
*   **Monitored Resources:** `PatientEncounter`, `PatientInvoice`
*   **OPA Configuration:** Multi-bundle concurrent polling.
*   **Service Port:** `8082` | **OPA Port:** `8182`

---

## 3. Environment Initialization

To test an environment, you must navigate into its specific directory (either `finance-microservice` or `clinic-modulith`). You cannot run both at the exact same time unless you open two separate terminals.

**1. Start the PostgreSQL Database and OPA Sidecar:**
Open a terminal in the folder of the environment you want to test and run:
```bash
docker-compose up -d
```
> **What does this do?** This command automatically downloads and spins up a local PostgreSQL database container and an OPA Sidecar container. You do *not* need to install PostgreSQL on your machine, and you do *not* need to manually create the database. The `docker-compose.yml` file automatically provisions the database (e.g., `finance_db` or `clinic_db`) and sets up the correct username/password for you.

**2. Boot the Spring Application:**
Next, you need to start the Spring Boot application so it can connect to the database you just created.

*   **If using an IDE (IntelliJ/Eclipse/VS Code):** Open the project and run the main class directly (Run `FinanceApplication.java` for the microservice, or `ClinicApplication.java` for the modulith).
*   **If using the Terminal:** Run the following command in the same directory:
```bash
mvn spring-boot:run
```

> **What does this do?** The Spring Boot app will boot up, connect to the PostgreSQL database, and use Hibernate to automatically generate all the necessary SQL tables (`authz_resource`, `authz_policy`, etc.). Then, the `StartupScanner` will automatically read your Java code and insert the condition fields into the database. Wait until you see the "Started Application" log in the console before proceeding.

---

## 4. Verification Workflow

The following workflow uses the **Finance Microservice** as the primary example, but the exact procedures apply to the Clinic Modulith (adjusting the port to `8082` and namespaces accordingly).

### 4.1. Phase 1: Validate Field Synchronization
Verify that the `StartupScanner` has successfully registered the application's domain fields, making them available to the UI's Condition Builder.

*   **Endpoint:** `GET http://localhost:8081/internal/authz/permissions/{permission_code}/fields`
*   **Success Criteria:** Returns a JSON array containing fields such as `amount`, `department`, `status`, and `requiresAudit`, including predefined dropdown enumerations.

### 4.2. Phase 2: Simulating Policy Creation
Simulate an Administrator submitting a complex authorization policy via the UI. 

**Business Rule:** 
> "Users holding the MANAGER role can create a Journal entry IF:
> The amount is under $5,000 AND the department is IT.
> OR
> The status is APPROVED AND audit is NOT required."

Execute the following `PUT` request to trigger the Rego compiler pipeline:

```http
PUT http://localhost:8081/internal/authz/policies
Content-Type: application/json

{
  "subjectType": "ROLE",
  "subjectId": "MANAGER",
  "namespace": "finance",
  "policies": [
    {
      "permissionCode": "finance:journal:create",
      "effect": "ALLOW",
      "enabled": true,
      "expressionJson": {
        "operator": "OR",
        "children": [
          {
            "operator": "AND",
            "children": [
              {"field": "amount", "comparison": "<", "value": 5000},
              {"field": "department", "comparison": "==", "value": "IT"}
            ]
          },
          {
            "operator": "AND",
            "children": [
              {"field": "status", "comparison": "==", "value": "APPROVED"},
              {"field": "requiresAudit", "comparison": "==", "value": false}
            ]
          }
        ]
      }
    }
  ]
}
```

*Architectural Note: Upon processing this request, `authz-core` immediately regenerates the Rego rules, archives them into `bundle.tar.gz`, and stores the payload in the database cache. Within 10 to 20 seconds, the adjacent OPA sidecar will automatically pull this updated bundle into memory.*

### 4.3. Phase 3: Runtime OPA Evaluation
Directly query the OPA Sidecar API to determine if the generated Rego rules accurately reflect the business logic.

#### Scenario A: Successful Authorization (First Condition Met)
*   **Condition:** Amount is 2500 (`< 5000`) and Department is `IT`.
*   **Request:**
```http
POST http://localhost:8181/v1/data/app/authz/finance/allow
Content-Type: application/json

{
  "input": {
    "subject": { "roles": ["MANAGER"] },
    "resource": {
      "amount": 2500,
      "department": "IT",
      "status": "DRAFT",
      "requiresAudit": true
    }
  }
}
```
*   **Expected Response:** `{"result": true}`

#### Scenario B: Denied Authorization (Threshold Exceeded)
*   **Condition:** Amount is 7500 (`> 5000`), forcing a denial on the first branch.
*   **Request:** Modify the previous payload `amount` to `7500`.
*   **Expected Response:** `{"result": false}`

#### Scenario C: Successful Authorization (Second Condition Met)
*   **Condition:** Despite being in the `HR` department with an exorbitant amount, the status is `APPROVED` and `requiresAudit` is `false`.
*   **Request:**
```http
POST http://localhost:8181/v1/data/app/authz/finance/allow
Content-Type: application/json

{
  "input": {
    "subject": { "roles": ["MANAGER"] },
    "resource": {
      "amount": 999999,
      "department": "HR",
      "status": "APPROVED",
      "requiresAudit": false
    }
  }
}
```
*   **Expected Response:** `{"result": true}`

---

## 5. Conclusion
Passing the scenarios detailed above confirms that the `authz-core` Bedrock 3-layer architecture successfully translates complex, nested front-end DTO structures into valid, executable Rego language syntax, achieving seamless local sidecar integration.
