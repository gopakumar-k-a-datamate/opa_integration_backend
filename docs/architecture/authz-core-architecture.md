# authz-core Library Architecture

This document explains how the `authz-core` library is internally structured, what conventions it follows, and the reasoning behind the key decisions. It is intended for developers who want to contribute to the library or understand how it fits together.

---

## 1. About Architecture

`authz-core` is designed as a **self-contained Spring Boot library** — not an application. It is embedded into consuming microservices or moduliths as a dependency. The architecture is **pragmatic and functional**: code is organized by **what it does** (service, compiler, jpa, rest), not by abstract layer labels.

The library avoids over-engineering. There are no mapper classes, no separate "application layer" packages, and no "adapter" wrappers beyond what is naturally needed to bridge Spring Data JPA to domain model objects. The goal is **simplicity, transparency, and direct dependency on concrete responsibilities**.

---

## 2. Engine-Agnostic Design (OPA Replaceability)

One of the key architectural decisions was to **not hard-couple the library to OPA**. OPA is the current policy engine, but the system is designed so that it can be replaced with another engine (e.g., Cedar, Casbin, a custom evaluator) without touching any consuming service code, domain model, or business service.

### How to Replace OPA

If OPA were replaced with a different engine in the future, we would handle this by **adding a new module** (e.g., `authz-cedar` or `authz-custom`) rather than heavily refactoring `authz-opa`. The new module would implement the core port interfaces (like `PolicyEvaluationClient` and `PolicyCompiler`), managing its own engine-specific code with minimal disruption. 

To switch the system over:
1. Create the new module (`authz-new-engine`) implementing `authz-core` contracts.
2. Update `bedrock-authz-starter` to pull in the new module instead of `authz-opa`.
3. `authz-core` and all consuming microservices remain **100% unchanged**.

> **This is why the current implementation module is named `authz-opa`** — the name makes the OPA-specificity explicit. The `authz-core` module name intentionally contains no reference to OPA, keeping the domain clean for future replacements.

---


## 3. Maven Module Structure

The library is split into **3 Maven modules**:

```
authz-core/                    ← Parent POM
├── authz-core/                ← Shared contracts: models, ports, annotations, DTOs, exceptions
├── authz-opa/                 ← All implementation: JPA, services, compiler, REST
└── bedrock-authz-starter/     ← Spring Boot auto-configuration + PolicyEnforcer
```

A consuming service adds only `bedrock-authz-starter` to its `pom.xml`. The other two modules are pulled in transitively.

> **Why 3 modules?** The split exists to enforce a clear dependency direction at compile time. `authz-opa` depends on `authz-core`, but `authz-core` never depends on `authz-opa`. A service class in `authz-opa` can import domain models and port interfaces from `authz-core` freely. The reverse is forbidden by Maven's module boundary — an `authz-core` class cannot accidentally import a JPA entity or Spring `@Service` from `authz-opa`.

---

## 4. `authz-core` Module — Shared Contracts

This module contains **everything that the library and the consuming service share**. It has no Spring dependencies. Any class here can be used by both the implementation side (`authz-opa`) and the consuming service side.

### Package Layout

```
org.datamate.authz/
├── annotation/         ← @PolicyResource, @PolicyField, FieldType
├── api/policy/         ← Port interfaces (6 interfaces)
├── dto/policy/         ← Request/response DTOs shared across REST and service layers
├── enforcement/        ← PolicyEnforcer interface, AuthorizationContext record
├── exception/          ← All library-specific exception types
└── model/policy/
    ├── entity/         ← Pure Java domain models (Resource, Permission, ConditionField, Policy)
    └── enumtype/       ← Enums: PolicyEffect, SubjectType, Status, FieldType
```

> **Note — No Spring in `authz-core`:** This module has zero Spring dependencies. Domain models are plain Java classes. The `PolicyEnforcer` interface and `AuthorizationContext` record are pure Java types. This means any class in `authz-core` can be instantiated and tested without a Spring context or a running application.

> **Note — `AuthorizationContext` is the engine-agnostic bridge:** `SpringSecurityPolicyEnforcer` builds an `AuthorizationContext` from the incoming request. `RestPolicyEvaluationClient` reads it and translates it into OPA's specific JSON format. Neither side knows about the other's implementation. If OPA is replaced, only the `PolicyEvaluationClient` implementation changes — the enforcer and all consuming services are untouched. See Section 2 for details.

---

## 5. `authz-opa` Module — All Implementation

This module contains every concrete implementation. It is organized by **technical responsibility**, not by abstract layer names:

```
org.datamate.authz/
├── jpa/
│   ├── entity/         ← JPA entity classes (with @Entity, @Column, @Version)
│   ├── repository/     ← Spring Data JpaRepository interfaces (query layer)
│   └── service/        ← Repository implementations that bridge Spring Data ↔ domain model
├── service/policy/     ← Business logic services (use-case classes)
├── compiler/
│   ├── ast/            ← AST node types: GroupNode, ConditionNode, LogicalOperator
│   ├── generator/      ← RegoGenerator: turns policies + AST into .rego text
│   └── AstBuilder.java ← Parses expressionJson into an AstNode tree
├── rest/
│   ├── controller/     ← REST controllers exposing /internal/authz/ endpoints
│   ├── client/         ← RestPolicyEvaluationClient: calls OPA sidecar
│   └── startup/        ← StartupPolicyCompiler: recompiles bundles on boot
└── model/              ← Additional model classes specific to the OPA implementation
```

> **Note — `jpa/service/` classes implement `api/policy/` interfaces from `authz-core`:** For example, `JpaPolicyRepository` implements `PolicyRepository`. The service classes in `service/policy/` only ever reference `PolicyRepository` (the interface) — they have no idea that Spring Data or any JPA entity exists. This is the only layer boundary enforced within `authz-opa`.

> **Note — Mapping is inline, not in separate mapper classes:** Each `jpa/service/` class contains its own `toDomain()` and `updateEntity()` private methods. There are no MapStruct-generated mappers or standalone mapper beans. This keeps each class self-contained and the mapping logic immediately visible next to the query that uses it.

> **Note — Services directly convert to DTOs when needed:** `GetConditionFieldsService.toDto()` converts `ConditionField` → `ConditionFieldDto` inline within the same service class. This avoids an unnecessary intermediate mapper class for simple conversions. If the conversion grows complex, it would be extracted — but by default, simple transformations live with the service that performs them.

> **Note — `service/policy/` classes are Spring `@Service` components:** They are annotated with `@Service` and `@Transactional`. They depend on the port interfaces from `authz-core` (injected via constructor injection). They are the only classes that contain authorization business logic. `DefaultPolicyCompiler.recompile()` is additionally `synchronized` to prevent concurrent compilations from interleaving bundle writes.

> **Note — REST controllers call services directly:** There is no intermediate "facade" or "application service" wrapping the service calls. A controller method calls `savePoliciesService.save(request)` directly and returns the response. Controllers are thin — they handle HTTP concerns only (status codes, headers, content-type).

> **Note — `BundleController` and `NamespaceController` are always active:** These are required by the OPA sidecar at runtime. `PolicyController` and `ConditionFieldController` are guarded by `@ConditionalOnProperty(datamate.authz.admin.enabled=true)` and are disabled by default to prevent accidental exposure of policy management endpoints in production environments where admin API access is not needed.

---

## 6. `bedrock-authz-starter` Module — Auto-Configuration

This is the entry point that consuming services add as their only `authz-core` dependency. It wires all the pieces together automatically via Spring Boot's auto-configuration mechanism.

```
org.datamate.authz.starter/
├── config/
│   ├── AuthzCoreAutoConfiguration.java   ← Scans services, compiler, enforcer
│   ├── AuthzJpaAutoConfiguration.java    ← Scans JPA layer; runs library Flyway migrations
│   └── AuthzRestAutoConfiguration.java   ← Scans REST controllers
└── enforcement/
    └── SpringSecurityPolicyEnforcer.java  ← Concrete PolicyEnforcer implementation
```

> **Note — Three separate `@AutoConfiguration` classes, not one:** `AuthzCoreAutoConfiguration` scans the service and compiler packages. `AuthzJpaAutoConfiguration` scans JPA and runs Flyway. `AuthzRestAutoConfiguration` scans REST controllers. This split means each layer can be individually disabled or overridden without affecting the others. For example, if a future consumer uses a non-Spring-Data persistence layer, they can set `authz.jpa.enabled=false` and provide their own `PolicyRepository` beans.

> **Note — The library runs its own Flyway migration separately:** `AuthzJpaAutoConfiguration` contains an inner `AuthzFlywayConfiguration` that runs `classpath:db/authz-migration` migrations against the consuming service's `DataSource`. It uses the table name `authz_flyway_schema_history` so it never collides with the service's own `flyway_schema_history`. The consuming service does not need to include the library's migration scripts in its own migration path.

> **Note — `SpringSecurityPolicyEnforcer` is the only concrete `PolicyEnforcer`:** The consuming service injects `PolicyEnforcer` (the interface from `authz-core`) — never `SpringSecurityPolicyEnforcer` directly. If the library's enforcement strategy ever changes, the consuming service code is unaffected.

---

## 7. Full Package Map

```
authz-core/
│
├── authz-core/  (org.datamate.authz)
│   ├── annotation/          @PolicyResource  @PolicyField  FieldType
│   ├── api/policy/          PolicyRepository  PermissionRepository  ResourceRepository
│   │                        ConditionFieldRepository  PolicyBundleCacheRepository
│   │                        PolicyCompiler  PolicyEvaluationClient
│   ├── dto/policy/          SavePoliciesRequest  PolicyItemRequest  PolicyGridItemDto  EvaluationPayload
│   ├── enforcement/         PolicyEnforcer  AuthorizationContext
│   ├── exception/           PolicyCompilationException  EngineConfigurationException
│   │                        InvalidPayloadException  StaleDataException  AuthzDeniedException
│   └── model/policy/
│       ├── entity/          Resource  Permission  ConditionField  Policy
│       └── enumtype/        PolicyEffect  SubjectType  Status  FieldType
│
├── authz-opa/  (org.datamate.authz)
│   ├── jpa/
│   │   ├── entity/          PolicyJpaEntity  PermissionJpaEntity  ResourceJpaEntity  ...
│   │   ├── repository/      SpringDataPolicyRepository  ...
│   │   └── service/         JpaPolicyRepository  JpaPermissionRepository  ...
│   ├── service/policy/      SavePoliciesService  GetPoliciesService  GetConditionFieldsService
│   │                        GetOpaBundleService  GetNamespacesService
│   │                        DefaultPolicyCompiler  TarGzBundleService
│   ├── compiler/
│   │   ├── ast/             GroupNode  ConditionNode  LogicalOperator
│   │   ├── generator/       RegoGenerator
│   │   └── AstBuilder
│   └── rest/
│       ├── controller/      PolicyController  ConditionFieldController
│       │                    BundleController  NamespaceController
│       ├── client/          RestPolicyEvaluationClient
│       └── startup/         StartupPolicyCompiler
│
└── bedrock-authz-starter/  (org.datamate.authz.starter)
    ├── config/              AuthzCoreAutoConfiguration  AuthzJpaAutoConfiguration
    │                        AuthzRestAutoConfiguration
    └── enforcement/         SpringSecurityPolicyEnforcer
```
