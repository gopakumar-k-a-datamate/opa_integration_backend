# Federated Policy-Based Authorization Workflow

## 1. Architectural Overview

Our system utilizes a **Federated Policy-Based Authorization** model powered by Open Policy Agent (OPA). Unlike traditional centralized authorization (where a single Identity Provider like Keycloak holds all permissions for all microservices), our architecture is completely **decentralized**.

Each microservice (or modulith domain) physically owns and manages its own authorization database tables, policy compilation logic, and its own dedicated OPA sidecar. This ensures that a microservice is entirely self-sufficient, highly available, and tightly coupled to its specific domain rules without cross-service network bottlenecks.

The library is split into three modules with distinct responsibilities:

| Module | Responsibility |
|---|---|
| `authz-core` | Domain models, port interfaces (`PolicyEnforcer`, `PolicyEvaluationClient`, `PolicyCompiler`), and annotations (`@PolicyResource`, `@PolicyField`) |
| `authz-opa` | OPA-specific implementations: Rego generator, bundle compiler (`DefaultPolicyCompiler`), REST controllers, and `RestPolicyEvaluationClient` |
| `bedrock-authz-starter` | Spring Boot auto-configuration and the `SpringSecurityPolicyEnforcer` bean |

---

## 2. End-to-End Workflow

The authorization workflow is divided into three distinct phases: **Schema Definition (Development)**, **Policy Authoring (Administration)**, and **Runtime Enforcement (Execution)**.

### Phase 1: Schema Definition (Database-First)
Developers define what can be protected and under what conditions.

1. **SQL Migrations:** A developer creates a Flyway SQL script (e.g., `V1__seed_finance_authz.sql`) within the microservice. This script inserts data into the `authz_resource`, `authz_permission`, and `authz_condition_field` tables. This establishes the **absolute source of truth** for what can be authorized.
2. **Code Annotations:** The developer annotates the corresponding Java Command object with `@PolicyResource` and `@PolicyField`. These act purely as **runtime markers** for the PEP — they do **not** auto-insert anything into the database.
3. **Application Boot:** When the microservice starts:
   - Flyway executes pending migrations, ensuring the database schema is synchronized with the developer's intent.
   - The `StartupPolicyCompiler` (from `authz-opa`) triggers immediately after the Spring context is ready. It reads all active namespaces from the database and calls `PolicyCompiler.recompile()` for each one, ensuring the OPA sidecar receives a fresh, up-to-date bundle — even if Flyway just applied schema changes.

### Phase 2: Policy Authoring & Compilation
Administrators define the rules, and the system translates them for OPA.

1. **Admin UI Configuration:** An administrator uses the Admin UI to assign permissions to users or roles (e.g., "Accountants can Create Journals"). They can also attach dynamic conditions (e.g., "Only if the Journal Amount is < $10,000").
2. **Database Storage:** The Admin UI saves these rules into the `authz_policy` table via `PUT /internal/authz/policies`. The conditions are stored as an Abstract Syntax Tree (AST) in JSON format.
3. **Rego Bundle Compilation:** `SavePoliciesService` triggers `DefaultPolicyCompiler.recompile()` after every save. The compiler:
   - First, synchronizes deprecated field flags across active policies.
   - Reads all enabled, non-deprecated policies for the target namespace.
   - Uses `RegoGenerator` to translate the JSON condition ASTs into native `.rego` policy code.
   - Packages the result as a `bundle.tar.gz` via `TarGzBundleService`.
   - Stores the bundle and its MD5 ETag in the `authz_policy_bundle_cache` table.
4. **OPA Polling:** The OPA sidecar continuously polls `GET /internal/authz/bundle/{namespace}`. The server uses the `If-None-Match` / `ETag` header to return `304 Not Modified` if nothing changed, saving bandwidth. When a change is detected, OPA downloads the new bundle into its in-memory cache.

### Phase 3: Runtime Enforcement
A user attempts to perform an action.

1. **Client Request:** A user makes an HTTP request to perform an action (e.g., creating a journal entry).

2. **Programmatic Enforcement:** Before executing business logic, the Application Service explicitly calls:
   ```java
   policyEnforcer.enforce(resource); // throws AccessDeniedException if denied
   ```
   The `PolicyEnforcer` is a port interface (`authz-core`). The implementation — `SpringSecurityPolicyEnforcer` — is registered automatically by `bedrock-authz-starter`.

   > **Note:** This is a **programmatic** model, not AOP. The application service deliberately calls `enforce()` at the appropriate point in its use case flow.

3. **Identity Extraction (Two-Tier Strategy):** `SpringSecurityPolicyEnforcer` resolves the current user using a two-tier fallback:
   - **Primary:** Reads `userId` and `roles` from Spring Security's `SecurityContextHolder` (populated when Spring Security JWT filters are fully configured).
   - **Fallback:** If the principal resolves to `anonymousUser` (e.g., in local development without a full JWT filter chain), the enforcer manually reads the `Authorization: Bearer <token>` header, base64-decodes the JWT payload, and extracts the `userId` and `role[]` fields directly.

4. **`AuthorizationContext` Construction:** The enforcer collects:
   - The `userId` and `roles` resolved in Step 3.
   - The `permissionCode` (built as `{namespace}:{resourceName}:{action}` from the `@PolicyResource` annotation).
   - The `resourceData` map (all fields annotated with `@PolicyField`, extracted via reflection).
   
   These are bundled into an engine-agnostic `AuthorizationContext` record.

5. **OPA Evaluation:** `RestPolicyEvaluationClient` (from `authz-opa`) translates the `AuthorizationContext` into OPA's specific JSON input format (`EvaluationPayload`) and sends it to the local OPA sidecar:
   ```
   POST {evaluation_url}    # Loaded from opa-config.yaml at startup
   {
     "input": {
       "user":       { "id": "...", "roles": ["ACCOUNTANT"] },
       "permission": "finance:journal:create",
       "resource":   { "amount": 2500, "department": "IT" }
     }
   }
   ```
   The `evaluation_url` is read from `opa-config.yaml` at application startup. If the URL cannot be loaded, `EngineConfigurationException` is thrown and the application refuses to start (**fail-fast**).

6. **Decision & Enforcement:**
   - OPA evaluates the JSON payload against its cached Rego bundle in memory and immediately returns `{"result": true}` or `{"result": false}`.
   - If `true`, `enforce()` returns normally and the business logic executes.
   - If `false` (or if OPA is unreachable — **fail-closed**), `enforce()` throws `AccessDeniedException`, which propagates as an **HTTP 403 Forbidden** response.

---

## 3. Summary of Core Components

| Component | Module | Responsibility |
|---|---|---|
| `authz_*` tables | Database | Single source of truth for resources, permissions, conditions, policies, and bundle cache |
| Flyway SQL migrations | Per-service | Manage lifecycle and versioning of the authorization schema (add, deprecate, delete resources) |
| `@PolicyResource` / `@PolicyField` | `authz-core` | Runtime markers on Command objects; used by the PEP to extract permission code and resource context |
| `PolicyEnforcer` | `authz-core` | Port interface for enforcement; decouples application services from the OPA implementation |
| `SpringSecurityPolicyEnforcer` | `bedrock-authz-starter` | Concrete implementation: extracts identity from Spring Security or JWT header, calls `PolicyEvaluationClient` |
| `PolicyEvaluationClient` / `RestPolicyEvaluationClient` | `authz-core` / `authz-opa` | Port + OPA adapter: maps `AuthorizationContext` → `EvaluationPayload`, HTTP POSTs to OPA sidecar |
| `DefaultPolicyCompiler` | `authz-opa` | Reads policies from DB, generates Rego via `RegoGenerator`, packages as `bundle.tar.gz`, stores ETag |
| `StartupPolicyCompiler` | `authz-opa` | On `ContextRefreshedEvent`, recompiles OPA bundles for all active namespaces from the current DB state |
| Admin UI | External | Frontend for administrators to map roles/users to permissions and author conditions |
| OPA Sidecar | Infrastructure | Ultra-fast, local policy engine; polls the bundle endpoint and evaluates `input` payloads in memory |
