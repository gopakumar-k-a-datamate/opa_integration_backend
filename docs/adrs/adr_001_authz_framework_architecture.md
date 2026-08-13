# ADR 001: Authz Framework Core Architecture

**Date:** August 13, 2026  
**Status:** Accepted  
**Context Area:** `authz-core`, `authz-opa`, `bedrock-authz-starter`

## 1. Context and Problem Statement

When building a reusable framework or library (such as the `authz` ecosystem), the architecture must prioritize extreme decoupling, strict technology agnosticism, and an intuitive developer experience. 

If a framework is too rigidly coupled to a specific implementation technology (e.g., OPA, JPA) or uses overly complex, fragmented internal structures (such as strict Hexagonal Ports/Adapters), it becomes brittle. It prevents consuming microservices from swapping out underlying engines in the future, and it forces developers to navigate a steep learning curve just to understand basic request flows.

**How should we structure the authorization framework to ensure it remains a pure, modular SDK while providing a clear, standard, and cohesive vision for developers maintaining its implementations?**

---

## 2. Architecture Decisions

To solve this, we have established the following architectural guidelines for the `authz` framework. Every developer working on this codebase must strictly adhere to these rules.

### Decision 1: SPI + Standard Layered (N-Tier) Architecture
We adopt a hybrid architecture to balance abstraction with developer familiarity.
- **The Core (`authz-core`):** Acts purely as a **Service Provider Interface (SPI)**. It contains *only* interfaces, POJO domain models, and domain Exceptions. 
- **The Implementations (e.g., `authz-opa`):** Provide the concrete logic. Because these modules are internal implementations, they must follow a highly familiar, strict **Layered (N-Tier)** structure using standard packages:
  - `.rest.controller` (API Endpoints)
  - `.rest.dto` (Data Transfer Objects)
  - `.service.[domain]` (Business Logic)
  - `.jpa.entity` (Database Mapping)
  - `.jpa.repository` (Data Access)
  - `.client.[integration]` (External System Integrations)

### Decision 2: 100% Technology-Agnostic Core API (`authz-core`)
The `authz-core` module is the framework's strict contract and canonical domain. It must be completely unaware of how policies are evaluated or persisted.

#### Domain Model Purity
- **Rule:** Domain models (e.g., `Policy`, `Permission`, `Resource`) residing in `authz-core` must remain pure Plain Old Java Objects (POJOs).
- **Rule:** Absolutely no infrastructural annotations are permitted inside the core domain models. This includes JPA annotations (`@Entity`, `@Table`) and Jackson annotations (`@JsonProperty`). Data mapping must happen exclusively in the implementation layer via dedicated DTOs or Entities.

#### SDK Contracts & Annotations
- **Rule:** The core module provides the public SDK (e.g., `PolicyEnforcer`) and domain annotations (e.g., `@ProtectedResource`) that consuming microservices interact with. Consumers should never directly autowire implementation-specific classes (like `OpaPolicyEvaluationClient`).
- **Rule:** Core interfaces and JavaDocs must use generic terminology (e.g., "Policy Engine" instead of "OPA", "Evaluation Payload" instead of "OPA JSON Input").

#### Strict Dependency Isolation
- **Rule:** The `authz-core` `pom.xml` must remain incredibly thin. It is strictly forbidden from importing heavy infrastructure libraries such as `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, or specific vendor SDKs. It should rely only on core Java and standard bedrock utilities.

### Decision 3: Strict Naming Conventions (No `Impl`)
To ensure a professional codebase, we explicitly ban the `Impl` suffix for services and adapters, as it provides zero descriptive value.
- **Interfaces:** Should be generic noun phrases defining the capability (e.g., `[Domain]Service`, `[Domain]Repository`, `[Capability]Client`).
- **Service Implementations:** Must describe their role. Use the `Default` prefix for standard business logic (e.g., `DefaultPolicyManagementService`).
- **Adapter Implementations:** Must prefix the technology they are adapting to instantly communicate their purpose (e.g., `JpaPolicyRepository`, `OpaPolicyEvaluationClient`).

### Decision 4: Unified Domain Services
To avoid severe code fragmentation, we explicitly reject the pattern of creating a single class/interface for every single CRUD operation.
- **Rule:** Highly cohesive operations belonging to the same domain must be grouped into a single, unified service interface (e.g., `PolicyManagementService` handling create, read, update, and delete actions for policies).

### Decision 5: Optional Technology Dependencies in the Starter
To ensure the framework remains decoupled at the auto-configuration level, concrete implementation dependencies must be optional.
- **Implementation:** In the `bedrock-authz-starter` POM, specific engine dependencies like `authz-opa` must include `<optional>true</optional>`.
- **Reasoning:** This allows a consuming microservice to include the bedrock starter but intentionally exclude the OPA implementation if they prefer to inject their own policy engine implementation (e.g., an AWS Cedar module), preventing classpath bloat and strict vendor lock-in.

---

## 3. Consequences

### Positive Developer Vision
- **Clear Mental Model:** Developers no longer need to navigate convoluted folders. The separation between "The Contract" (`authz-core`) and "The Implementation" (`authz-opa` via N-Tier) is crystal clear.
- **True Portability:** Because `authz-core` is purely abstract, we can build an entirely new module (e.g., `authz-cedar`) tomorrow, and all microservices depending on `authz-core` contracts will continue to function seamlessly without rewriting code.
- **Self-Documenting Code:** By adopting precise naming conventions (like `JpaPolicyRepository` or `OpaPolicyEvaluationClient`), developers immediately understand exactly *how* a class implements an interface without needing to open the file.

### Constraints
- Code reviews must strictly enforce these boundaries. Any Pull Request that introduces infrastructural annotations into the core module, uses an `Impl` suffix, or fragments services unnecessarily must be rejected to preserve the integrity of the framework.
