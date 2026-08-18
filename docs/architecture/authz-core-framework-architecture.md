# Authz Framework Core Architecture

**Date:** August 13, 2026  
**Context Area:** `authz-core`, `authz-opa`, `bedrock-authz-starter`

## 1. Context and Problem Statement

When building a reusable framework or library (such as the `authz` ecosystem), the architecture must prioritize extreme decoupling, strict technology agnosticism, and an intuitive developer experience.

If a framework is too rigidly coupled to a specific implementation technology (e.g., OPA, JPA) or uses overly complex, fragmented internal structures (such as strict Hexagonal Ports/Adapters), it becomes brittle. It prevents consuming microservices from swapping out underlying engines in the future, and it forces developers to navigate a steep learning curve just to understand basic request flows.

---

## 2. Engine-Agnostic Design (OPA Replaceability)

One of the key architectural requirements was to **not hard-couple the library to OPA**. OPA is the current policy engine, but the system is designed so that it can be replaced with another engine (e.g., Cedar, Casbin, a custom evaluator) without touching any consuming service code, domain model, or business service.

### How to Replace OPA
If OPA were replaced with a different engine in the future, we would handle this by **adding a new module** (e.g., `authz-cedar` or `authz-custom`) rather than heavily refactoring `authz-opa`. The new module would implement the core SPI interfaces (like `PolicyValidation` and `PolicyCompiler`), managing its own engine-specific code with minimal disruption.

To switch the system over:
1. Create the new module (`authz-new-engine`) implementing `authz-core` contracts.
2. Update `bedrock-authz-starter` to pull in the new module instead of `authz-opa`.
3. `authz-core` and all consuming microservices remain **100% unchanged**.

> **This is why the current implementation module is named `authz-opa`** — the name makes the OPA-specificity explicit. The `authz-core` module name intentionally contains no reference to OPA, keeping the domain clean for future replacements.

---

## 3. Maven Module Structure

The library is split into **3 Maven modules** to enforce a clear dependency direction at compile time:

```text
authz-core-parent/             ← Parent POM
├── authz-core/                ← Shared contracts: models, SPIs, annotations, DTOs
├── authz-opa/                 ← All implementation: JPA, services, compiler, REST
└── bedrock-authz-starter/     ← Spring Boot auto-configuration + PolicyEnforcer
```

A consuming service adds only `bedrock-authz-starter` to its `pom.xml`. The other two modules are pulled in transitively.
`authz-opa` depends on `authz-core`, but `authz-core` never depends on `authz-opa`. A service class in `authz-opa` can import domain models and interfaces from `authz-core` freely. The reverse is forbidden by Maven's module boundary.

---

## 4. Architecture Decisions

To solve the coupling and fragmentation problems of the past, we have established the following architectural guidelines. Every developer working on this codebase must strictly adhere to these rules.

### Decision 1: SPI + Standard Layered (N-Tier) Architecture
We adopt a hybrid architecture to balance abstraction with developer familiarity.
- **The Core (`authz-core`):** Acts purely as a **Service Provider Interface (SPI)**. It contains *only* interfaces, POJO domain models, and domain Exceptions.
- **The Implementations (e.g., `authz-opa`):** Provide the concrete logic. Because these modules are internal implementations, they must follow a highly familiar, strict **Layered (N-Tier)** structure using standard packages:
    - `.rest.controller` (API Endpoints)
    - `.service.[domain]` (Business Logic)
    - `.jpa.entity` (Database Mapping)
    - `.jpa.repository` (Data Access)
    - `.client` (External System Integrations)

### Decision 2: 100% Technology-Agnostic Core API (`authz-core`)
The `authz-core` module is the framework's strict contract and canonical domain. It must be completely unaware of how policies are evaluated or persisted.

#### Domain Model Purity
- **Rule:** Domain models (e.g., `Policy`, `Permission`) residing in `authz-core` must remain pure Plain Old Java Objects (POJOs).
- **Rule:** Absolutely no infrastructural annotations are permitted inside the core domain models. This includes JPA annotations (`@Entity`, `@Table`) and Jackson annotations (`@JsonProperty`). Data mapping must happen exclusively in the implementation layer via dedicated DTOs or Entities.

#### SDK Contracts & Annotations
- **Rule:** The core module provides the public SDK (e.g., `PolicyEnforcer`) and domain annotations (e.g., `@ProtectedResource`) that consuming microservices interact with. Consumers should never directly autowire implementation-specific classes.
- **Rule:** Core interfaces and JavaDocs must use generic terminology (e.g., "Policy Engine" instead of "OPA").

#### Strict Dependency Isolation
- **Rule:** The `authz-core` `pom.xml` must remain incredibly thin. It is strictly forbidden from importing heavy infrastructure libraries such as `spring-boot-starter-web` or `spring-boot-starter-data-jpa`. It should rely only on core Java and standard bedrock utilities.

### Decision 3: Strict Naming Conventions (No `Impl`)
To ensure a professional codebase, we explicitly ban the `Impl` suffix for services and adapters, as it provides zero descriptive value.
- **Interfaces:** Should be generic noun phrases defining the capability (e.g., `PolicyManagementService`, `PolicyRepository`).
- **Service Implementations:** Must describe their role. Use the `Default` prefix for standard business logic (e.g., `DefaultPolicyManagementService`).
- **Adapter Implementations:** Must prefix the technology they are adapting to instantly communicate their purpose (e.g., `JpaPolicyRepository`, `OpaPolicyEvaluationClient`).

### Decision 4: Unified Domain Services
To avoid severe code fragmentation, we explicitly reject the Hexagonal pattern of creating a single class/interface for every single CRUD operation.
- **Rule:** Highly cohesive operations belonging to the same domain must be grouped into a single, unified service interface (e.g., `PolicyManagementService` handling create, read, update, and delete actions for policies).

### Decision 5: The "Exclusion Pattern" for Default Engines in the Starter
To maximize developer convenience while preserving flexibility, the starter provides a sensible default engine out-of-the-box rather than forcing developers to explicitly configure one.
- **Implementation:** In the `bedrock-authz-starter` POM, the default engine (`authz-opa`) is included as a standard, non-optional transitive dependency.
- **Reasoning (Convention over Configuration):** 99% of microservices will use the default OPA engine. By making it a standard dependency, microservice developers only need to import `bedrock-authz-starter` for a fully working setup. If a microservice needs to swap to a different engine in the future (e.g., an AWS Cedar module), they can simply use Maven's `<exclusions>` tag to exclude `authz-opa` from the starter and import their engine of choice. This prevents classpath bloat for edge cases without burdening the majority.

---

## 5. Full Package Map (Current State)

```text
authz-core/
│
├── authz-core/  (org.datamate.authz)
│   ├── annotation/          @ProtectedResource  @PolicyField
│   ├── api/policy/          PolicyRepository  PolicyCompiler  PolicyManagementService
│   │                        PolicyValidation  PolicyEvaluationClient
│   ├── dto/policy/          SavePoliciesRequest  ConditionFieldDto
│   ├── enforcement/         PolicyEnforcer  AuthorizationContext  DefaultPrincipalProvider
│   ├── exception/           PolicyCompilationException  AuthzDeniedException
│   └── model/policy/
│       ├── entity/          Resource  Permission  ConditionField  Policy
│       └── enumtype/        PolicyEffect  SubjectType  Status  FieldType
│
├── authz-opa/  (org.datamate.authz)
│   ├── jpa/
│   │   ├── entity/          PolicyJpaEntity  PermissionJpaEntity  ResourceJpaEntity
│   │   └── repository/      JpaPolicyRepository  PolicyJpaRepository (Spring Data interface)
│   ├── service/policy/      DefaultPolicyManagementService  DefaultPolicyCompiler  TarGzBundleService
│   ├── compiler/            RegoGenerator  AstBuilder
│   ├── client/              OpaPolicyEvaluationClient  OpaPolicyValidator
│   └── rest/
│       ├── controller/      PolicyController  ConditionFieldController  BundleController
│       ├── client/          RestPolicyEvaluationClient
│       └── startup/         StartupPolicyCompiler
│
└── bedrock-authz-starter/  (org.datamate.authz.starter)
    ├── config/              AuthzCoreAutoConfiguration  AuthzJpaAutoConfiguration  AuthzRestAutoConfiguration
    └── enforcement/         SpringSecurityPolicyEnforcer
```

---

## 6. Consequences

### Positive Developer Vision
- **Clear Mental Model:** Developers no longer need to navigate convoluted folders. The separation between "The Contract" (`authz-core`) and "The Implementation" (`authz-opa` via N-Tier) is crystal clear.
- **True Portability:** Because `authz-core` is purely abstract, we can build an entirely new module (e.g., `authz-cedar`) tomorrow, and all microservices depending on `authz-core` contracts will continue to function seamlessly without rewriting code.
- **Self-Documenting Code:** By adopting precise naming conventions (like `JpaPolicyRepository` or `OpaPolicyEvaluationClient`), developers immediately understand exactly *how* a class implements an interface without needing to open the file.
