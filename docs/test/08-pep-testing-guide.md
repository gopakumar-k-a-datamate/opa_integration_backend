# 08 - PEP End-to-End Testing Guide

This guide explains how to manually test the **Policy Enforcement Point (PEP)** that was implemented in the Application Layer using Spring AOP.

## Prerequisites

Before testing, ensure you have Docker installed (for PostgreSQL and OPA) and Java 21 / Maven configured.

---

## 1. Boot the Environment

Open a terminal and navigate to the `finance-microservice` directory:
```bash
cd c:\Projects\Personal\opa_integration_backend\finance-microservice
```

Start the infrastructure (Database & OPA Sidecar):
```bash
docker-compose up -d
```

Start the Spring Boot microservice:
```bash
mvn spring-boot:run
```

Wait until you see the `Started FinanceApplication` log message.

---

## 2. Simulate the Admin UI (Create a Policy)

We need to tell `authz-core` what the rules are for the `ACCOUNTANT` role. We will create a policy that says **"An Accountant can only create a Journal if the amount is less than 5000"**.

Open a new terminal or use Postman to send this request to the local microservice:

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
      "enabled": true,
      "expressionJson": {
        "operator": "AND",
        "children": [
          {"field": "amount", "comparison": "<", "value": 5000}
        ]
      }
    }
  ]
}'
```

*Wait about 10-15 seconds.* The OPA Sidecar polls the `/internal/authz/bundle/finance` endpoint periodically. It will automatically download the newly compiled Rego rules.

---

## 3. Trigger the PEP Interceptor

We will now hit the `JournalController` (`POST /api/journals`). 
The Controller will map your JSON payload into a `Journal` Command object and pass it down to the `JournalService` in the Application Layer. 
Because the Command is annotated with `@PolicyResource`, the `PolicyEnforcementAspect` will **instantly intercept the Service call** and query OPA before allowing the Use Case to execute!

### Test Case A: The Unauthorized Request (Fails Policy)

Let's attempt to create a journal with an amount of **9000** (which violates our `< 5000` rule).

```bash
curl -X POST http://localhost:8081/api/journals \
-H "Content-Type: application/json" \
-d '{
  "amount": 9000,
  "department": "IT"
}'
```

**Expected Result:**
The PEP blocks the request before it ever reaches the controller! Spring Security will return a `403 Forbidden` response:
```json
{
  "timestamp": "...",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied: You do not have permission to perform this action.",
  "path": "/api/journals"
}
```

### Test Case B: The Authorized Request (Passes Policy)

Now let's send a valid amount of **2500**:

```bash
curl -X POST http://localhost:8081/api/journals \
-H "Content-Type: application/json" \
-d '{
  "amount": 2500,
  "department": "IT"
}'
```

**Expected Result:**
OPA evaluates the rules and returns `true`. The PEP steps aside and allows the controller to execute. You will receive:
```json
{
  "message": "OPA approved the request. Controller logic executed successfully!",
  "status": "SUCCESS"
}
```

---

## 4. Notes on Identity Extraction

For testing purposes in a fresh environment, if Spring Security is not configured to extract a JWT, the PEP might identify you as an `"anonymous"` user with no roles. 

If this happens and you get a 403 even for Test Case B, you can easily bypass the JWT requirement by temporarily hardcoding the roles in `PolicyEnforcementAspect.java`:
```java
List<String> roles = List.of("ACCOUNTANT"); // Hardcoded for local testing
```
Make sure to revert this once your API Gateway and JWT Filters are fully established!
