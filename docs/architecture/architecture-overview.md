# Federated Policy-Based Authorization using OPA

## Goal
Transition from static **Role → Permission** mappings to dynamic, business-rule-based **Policies**, keeping **OPA (Open Policy Agent)** as the Policy Decision Point (PDP). 
To ensure true microservice scalability and loose coupling, this architecture uses a **Federated Authorization Library** model, where the Identity module acts strictly as an Identity Provider, and each application service manages its own authorization state.

---

## High-Level Architecture (Federated Model)

```mermaid
flowchart TB
    subgraph AdminUI["Admin UI / API Gateway"]
        RoleGrid["Role-Permission Grid"]
        CondBuilder["Condition Builder"]
    end

    subgraph IdentityModule["Identity Module (IdP)"]
        UserDB[("User / Role DB")]
        RoleAPI["Role API (Read-Only)"]
        EventPub["Event Publisher"]
    end
    
    RabbitMQ(("RabbitMQ\nFanout Exchange\n'auth.subject.sync'"))

    subgraph ApplicationService["Application Service (e.g., Finance)"]
        PolicyAPI["Policy CRUD API (REST)"]
        BundleAPI["Bundle Serving API (REST)"]
        SubjectAPI["Subject API (REST)"]
        
        subgraph AuthzLibrary["authz-core (Shared Library)"]
            ManagementService["Policy Management"]
            Compiler["Policy Compiler"]
            SubjectSync["Subject Updater"]
        end
        PEP["PEP (Policy Enforcement Point)"]
        LocalAuthzDB[("Local Authz Tables")]
    end

    subgraph Runtime["Runtime"]
        OPA["Local OPA Sidecar"]
    end

    AdminUI -->|"1. fetch subjects"| SubjectAPI
    AdminUI -->|"2. manage policies"| PolicyAPI
    PolicyAPI -->|"delegates to"| ManagementService
    SubjectAPI -->|"delegates to"| ManagementService
    ManagementService -->|read/write| LocalAuthzDB
    ManagementService -->|on save| Compiler
    Compiler -->|reads policies| LocalAuthzDB
    Compiler -->|writes bundle| LocalAuthzDB
    OPA -->|polls| BundleAPI
    BundleAPI -->|reads| LocalAuthzDB
    ApplicationService -->|"check permission"| PEP
    PEP -->|query| OPA
    
    IdentityModule -->|"state change"| EventPub
    EventPub -->|"publish (Async)"| RabbitMQ
    RabbitMQ -->|"consume"| SubjectSync
    SubjectSync -->|"upsert (Local DB)"| LocalAuthzDB
```

---

## Core Concepts

The system is built on a **Federated Library** that encapsulates authorization logic. It is embedded in every application module/microservice.

### How They Work Together

1. **Identity Provider:** The central Identity module only manages Users, standard Roles (e.g., `ACCOUNTANT`), and User-Role assignments. It publishes `AuthzSubjectSyncEvent`s to a RabbitMQ fanout exchange on any state change.
2. **Shared Library (`authz-core`):** A reusable dependency injected into application services. It provisions local database tables and exposes standard REST APIs for the Admin UI and OPA.
3. **Subject Sync:** The application service consumes the Identity Provider's events and delegates them to the `authz-core` library, maintaining a local `authz_subject` table. This provides a highly available, read-only projection of Identity subjects for the Admin UI policy builder.
4. **Local Resources:** An application service defines its own **Resources** (grouped by a `namespace` for bounded context), **Permissions**, and **Condition Fields** via annotations on its Commands.
5. **Database-First Migrations:** The application uses Flyway SQL scripts to define its resources and permissions in the **local** database schema. No central sync is required.
6. **Local Policies:** A **Policy** ties a local Permission to a standard Role or User (validated against the local `authz_subject` table) and adds dynamic conditions. The `authz-core` library compiles these policies into Rego and serves the bundle to the local OPA sidecar.

### Entity Relationship Diagram (per Application Database)

```mermaid
erDiagram
    RESOURCE ||--o{ PERMISSION : has
    PERMISSION ||--o{ CONDITION_FIELD : defines
    PERMISSION ||--o{ POLICY : "governed by"
    POLICY_BUNDLE_CACHE ||--o{ POLICY : "caches (per namespace)"
    AUTHZ_SUBJECT ||--o{ POLICY : "references (User or Role)"
```
*(Note: Roles and Users are managed externally in the Identity Provider, so the local Policy table simply stores the `role_name` or `user_id` as a reference).*

---

## Key Design Decisions

| Decision | Rationale |
|---|---|
| **Federated Library Model** | The Identity module does not own policies. Each application manages its own authz state via the `authz-core` library, ensuring perfect loose coupling and zero central bottlenecks. |
| **Database-First Schema** | Flyway SQL migrations define the authorization metadata, while `@PolicyResource` and `@PolicyField` annotations act purely as runtime markers for OPA evaluation. |
| **Event-Driven Subject Sync** | Instead of synchronous cross-service queries to fetch user/role data for Admin UI dropdowns and validation, subjects are replicated into each local DB via RabbitMQ. This prevents the Identity Service from being a single point of failure during Policy Management. |
| **JSON AST for conditions** | Normalized DB tables for nested AND/OR groups are overly complex. JSON maps perfectly to UI rule builders. |
| **DENY overrides ALLOW** | Any matching DENY policy blocks access regardless of ALLOW policies. Enforced via `not deny_rule` in Rego. |
| **Local OPA Bundle Cache** | The `authz-core` library compiles Rego and stores zipped bundles *per namespace* in the local database, serving them directly to the local OPA sidecar. |
| **Soft deletes on all entities** | All tables use `deleted_at` timestamp. `NULL` = active. |

---

## Document Index

| Document | Contents |
|---|---|
| [authz-core-architecture.md](./authz-core-architecture.md) | Internal library architecture: Bedrock 3-layer model, module structure, port interfaces, design decisions |
| [database-schema.md](./database-schema.md) | Identity Schema vs. Library Schema with column definitions |
| [policy-engine.md](./policy-engine.md) | Condition engine, local field registry, local deprecation handling |
| [opa-integration.md](./opa-integration.md) | Local compiler pipeline, Rego generation, OPA deployment |
| [ADR001-admin-ui-workflow.md](../adrs/ADR001-admin-ui-workflow.md) | Modular Role-permission grid, condition builder |
| [ADR002-api-endpoints.md](../adrs/ADR002-api-endpoints.md) | REST APIs exposed by the authz-core library |
