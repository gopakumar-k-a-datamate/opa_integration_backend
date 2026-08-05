# 00. Federated Policy-Based Authorization Workflow

## 1. Architectural Overview

Our system utilizes a **Federated Policy-Based Authorization** model powered by Open Policy Agent (OPA). Unlike traditional centralized authorization (where a single Identity Provider like Keycloak holds all permissions for all microservices), our architecture is completely **decentralized**. 

Each microservice (or modulith domain) physically owns and manages its own authorization database tables, policy compilation logic, and its own dedicated OPA sidecar. This ensures that a microservice is entirely self-sufficient, highly available, and tightly coupled to its specific domain rules without cross-service network bottlenecks.

---

## 2. End-to-End Workflow

The authorization workflow is divided into three distinct phases: **Schema Definition (Development)**, **Policy Authoring (Administration)**, and **Runtime Enforcement (Execution)**.

### Phase 1: Schema Definition (Database-First)
Developers define what can be protected and under what conditions.

1. **SQL Migrations:** A developer creates a Flyway SQL script (e.g., `V1__seed_finance_authz.sql`) within the microservice. This script inserts data into the `authz_resource`, `authz_permission`, and `authz_condition_field` tables. This establishes the absolute source of truth for what can be authorized.
2. **Code Annotations:** The developer annotates the corresponding Java Command object with `@PolicyResource` and `@PolicyField`. These act purely as runtime markers.
3. **Application Boot:** When the microservice boots, Flyway executes the migrations, ensuring the database schema is perfectly synchronized with the developer's intent.

### Phase 2: Policy Authoring & Compilation
Administrators define the rules, and the system translates them for OPA.

1. **Admin UI Configuration:** An administrator uses the Admin UI to assign permissions to users or roles (e.g., "Accountants can Create Journals"). They can also attach dynamic conditions (e.g., "Only if the Journal Amount is < $10,000").
2. **Database Storage:** The Admin UI saves these rules into the `authz_policy` table. The conditions are stored as an Abstract Syntax Tree (AST) in JSON format.
3. **Rego Bundle Compilation:** The Java application uses a `RegoGenerator` to read the `authz_policy` table and dynamically compile the JSON ASTs into native `.rego` policy code.
4. **OPA Polling:** The OPA sidecar continuously polls the microservice (e.g., via `/internal/authz/bundle/finance`). When a change is detected, OPA downloads the newly compiled Rego bundle into its in-memory cache.

### Phase 3: Runtime Enforcement
A user attempts to perform an action.

1. **Client Request:** A user makes an HTTP request to the microservice to perform an action (e.g., creating a journal).
2. **AOP Interception:** The Spring Boot application catches the incoming Command object using an Aspect-Oriented Programming (AOP) interceptor (`PolicyEnforcementAspect`).
3. **Payload Construction:** The interceptor extracts:
   - The user's Identity and Roles (from the Security Context/JWT).
   - The Action being performed (from the `@PolicyResource` annotation).
   - The exact data being operated on (from the `@PolicyField` annotations on the command fields).
4. **Local OPA Evaluation:** The Java application wraps this data into an `OpaInputPayload` and sends a lightning-fast HTTP `POST` request (via `OpaRestTemplateAdapter`) to the local OPA sidecar running on `localhost`.
5. **Decision:** OPA evaluates the JSON payload against its cached Rego bundle in memory. It immediately returns `true` (allow) or `false` (deny).
6. **Execution or Rejection:** 
   - If `true`, the interceptor allows the Spring controller to proceed with business logic.
   - If `false`, the interceptor immediately throws an `AccessDeniedException` (HTTP 403 Forbidden).

---

## 3. Summary of Core Components

*   **Database (`authz_*` tables):** The single source of truth for resources, permissions, and raw policies.
*   **Flyway:** Manages lifecycle, versioning, and deprecation of the authorization schema.
*   **Bedrock Authz Starter:** The shared Java library containing the AOP interceptors, Rego generators, and JPA repositories.
*   **Admin UI:** The frontend for administrators to map roles to permissions and author conditions.
*   **OPA Sidecar:** The ultra-fast, local policy engine that evaluates requests against the compiled Rego bundles without ever talking to the central database or network.
