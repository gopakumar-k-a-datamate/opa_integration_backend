# Authz-Core Integration Testing Guide

This guide is designed for developers who are new to our federated authorization workflow. It provides a step-by-step tutorial on how to test the end-to-end working of the integrated `authz-core` module within a single application.

We will use the **Finance Microservice (`finance-microservice`)** as our testing environment. 

By following these steps, you will see exactly how the Admin UI fetches data, how policies are saved and compiled, how OPA receives the rules, and finally, how the application's Policy Enforcement Point (PEP) intercepts and authorizes live requests.

---

## 1. Booting the Environment

First, you need to start the environment. The microservice needs a PostgreSQL database and an OPA sidecar.

1. **Start the Infrastructure:** Open a terminal in the `finance-microservice` directory and start the database and sidecar containers.
   ```bash
   cd finance-microservice
   docker-compose up -d
   ```
2. **Boot the Application:** Start the Spring Boot application (which includes the `authz-core` library).
   ```bash
   mvn spring-boot:run
   ```
   *Wait until you see the "Started FinanceApplication" log.* During startup, the library automatically creates the authorization tables and scans your code for `@PolicyResource` annotations to populate them.

---

## 2. Step-by-Step API Flow

The following steps simulate the chronological flow of data, starting from what the Admin UI would do, down to the final runtime authorization check.

### Step 1: Check Current Policies (Simulate UI Load)
Before an admin creates a policy, the UI fetches the existing permission matrix for a specific role (e.g., `ACCOUNTANT`).

**Endpoint:** `GET /internal/authz/policies?subjectType=ROLE&subjectId=ACCOUNTANT`

**Why we need this:** This proves that the local microservice correctly exposes the `authz-core` endpoints and can query the local database for policies governing the `ACCOUNTANT` role. On a fresh boot, this will likely return policies with `effect: null`, indicating no rules have been set yet.

```bash
curl "http://localhost:8081/internal/authz/policies?subjectType=ROLE&subjectId=ACCOUNTANT"
```

### Step 2: Discover Condition Fields (Condition Builder)
To build a rule (e.g., "Amount < 5000"), the Admin UI needs to know what fields are available for the `finance:journal:create` permission.

**Endpoint:** `GET /internal/authz/permissions/finance:journal:create/fields`

**Why we need this:** This verifies that the startup scanner correctly registered the fields defined in your Java code (like `amount` and `department`) into the local database, making them available to the UI condition builder.

```bash
curl http://localhost:8081/internal/authz/permissions/finance:journal:create/fields
```

### Step 3: Create a Policy (Save & Compile)
Simulate the Administrator clicking "Save" on the UI to define a new rule: "An Accountant can create a journal IF the amount is less than 5000".

**Endpoint:** `PUT /internal/authz/policies`

**Why we need this:** This is the most critical step. When you send this payload, `authz-core` saves the rule to the database, instantly compiles it into executable Rego code, and zips it into an OPA bundle (`bundle.tar.gz`). 

**Working Payload:**
```bash
curl -X PUT http://localhost:8081/internal/authz/policies \
-H "Content-Type: application/json" \
-d '{
  "subjectType": "ROLE",
  "subjectId": "ACCOUNTANT",
  "namespace": "finance",
  "policies": [
    {
      "permissionCode": "finance:journal:create",
      "effect": "ALLOW",
      "expressionJson": {
        "operator": "AND",
        "children": [
          {"field": "amount", "comparison": "<", "value": 5000}
        ]
      },
      "enabled": true,
      "isDeleted": false
    }
  ]
}'
```

### Step 4: Verify Bundle Generation (Optional Debugging)
The OPA sidecar will automatically poll for the new rules, but you can manually verify that the bundle was generated successfully.

**Endpoint:** `GET /internal/authz/bundle/finance`

**Why we need this:** If OPA isn't enforcing your rules, you can download the bundle directly from this endpoint and inspect the generated `.rego` files to debug the compiler's output.

```bash
curl -O -J http://localhost:8081/internal/authz/bundle/finance
```

### Step 5: Verify OPA Runtime (Sidecar Evaluation)
Within 10-20 seconds of Step 3, the local OPA sidecar (running on port `8181`) will have downloaded the new bundle. Let's ask OPA directly if a journal of $2500 is allowed.

**Endpoint:** `POST http://localhost:8181/v1/data/app/authz/finance/allow`

**Why we need this:** This tests the Rego logic in isolation, completely bypassing the Java application. It proves that OPA loaded the bundle and that the logic correctly evaluates the input.

```bash
curl -X POST http://localhost:8181/v1/data/app/authz/finance/allow \
-H "Content-Type: application/json" \
-d '{
  "input": {
    "subject": { "roles": ["ACCOUNTANT"] },
    "resource": { "amount": 2500, "department": "IT" }
  }
}'
```
*Expected Output:* `{"result": true}`

### Step 6: Verify PEP Interceptor (End-to-End API Call)
Finally, we test the actual application endpoint to ensure the Policy Enforcement Point (PEP) is working.

**Endpoint:** `POST /api/journals`

**Why we need this:** This proves the entire flow. When you hit the business API, the PEP aspect automatically intercepts the call, builds the input payload, queries OPA, and either allows or blocks execution.

> **Identity Mocking Note:** Because this is a fresh test environment without an API Gateway passing real JWTs, you may need to temporarily hardcode the `ACCOUNTANT` role in the `PolicyEnforcementAspect.java` so the PEP knows who you are. For instructions on how to mock this identity, please see the [PEP End-to-End Testing Guide](./pep-testing-guide.md#4-notes-on-identity-extraction).

**Test 1: Unauthorized Request (Fails Policy)**
```bash
curl -X POST http://localhost:8081/api/journals \
-H "Content-Type: application/json" \
-d '{ "amount": 9000, "department": "IT" }'
```
*Expected Output:* `403 Forbidden` (because 9000 is not `< 5000`).

**Test 2: Authorized Request (Passes Policy)**
```bash
curl -X POST http://localhost:8081/api/journals \
-H "Content-Type: application/json" \
-d '{ "amount": 2500, "department": "IT" }'
```
*Expected Output:* Success response from the controller.

For a deeper dive into testing the PEP, controller mocking, and application-level interception, refer to the [PEP End-to-End Testing Guide](./pep-testing-guide.md).
