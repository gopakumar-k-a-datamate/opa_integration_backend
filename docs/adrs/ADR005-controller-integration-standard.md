# Authz-OPA Library Integration Guide

This guide explains how to integrate the `authz-opa` library into your microservices or modular monolith using a strict **Clean Architecture / Hexagonal Architecture** approach.

## 🏗️ Architecture Overview

The `authz-opa` library provides core authorization logic through **Inbound Ports (Use Cases)**. It does not provide REST Controllers.

Instead of building dedicated REST controllers just to wrap these use cases, the consuming application can inject the library's Use Cases directly into its own domain services (e.g., a `RoleManagementService` or `UserOnboardingService`).

### Library Boundaries

*   **Inbound Ports (`*UseCase`)**: Interfaces defining the capabilities the library offers (e.g., `SavePoliciesUseCase`).
*   **Application Services (`*Service`)**: The concrete implementations of those use cases inside the library, responsible for orchestration and application-level validation.
*   **Outbound Ports (`*Port`)**: Interfaces defining what the library needs from external systems or the consuming application (e.g., `PolicyRepositoryPort`, `SubjectValidationPort`).

---

## 💻 Example Integration

### 1. Inside the `authz-opa` Library

The library defines the use case and implements the orchestration and validation within its own Service layer. If it needs to validate something against the consumer's database (like checking if a subject exists), it uses an **Outbound Port**.

```java
// --- Inbound Port ---
public interface SavePoliciesUseCase {
    void savePolicies(SavePoliciesRequest request);
}

// --- Outbound Ports ---
public interface PolicyRepositoryPort {
    void save(Policy policy);
}

public interface PolicyCompilerPort {
    void compile(String namespace);
}

public interface SubjectValidationPort {
    boolean subjectExists(SubjectType type, String subjectId);
}

// --- Application Service Implementation ---
@Service
@RequiredArgsConstructor
public class SavePoliciesService implements SavePoliciesUseCase {

    private final SubjectValidationPort subjectValidationPort;
    private final PolicyRepositoryPort policyRepository;
    private final PolicyCompilerPort policyCompilerPort;

    @Override
    @Transactional
    public void savePolicies(SavePoliciesRequest request) {
        
        // 1. Application-level validation using outbound ports
        if (!subjectValidationPort.subjectExists(request.getSubjectType(), request.getSubjectId())) {
            throw new IllegalArgumentException("Subject does not exist");
        }

        // 2. Core domain logic / saving
        // policyRepository.save(...);

        // 3. Orchestration
        // policyCompilerPort.compile(request.getNamespace());
    }
}
```

### 2. Inside the Consuming Microservice

The consuming microservice should create its own domain service that wraps the library's inbound ports. This provides a dedicated layer where the microservice can execute its own consumer-specific validations before calling the library.

```java
package org.datamate.pharmacy.application.service;

import lombok.RequiredArgsConstructor;
import org.datamate.authz.application.port.in.GetConditionFieldsUseCase;
import org.datamate.authz.application.port.in.GetNamespacesUseCase;
import org.datamate.authz.application.port.in.GetPoliciesUseCase;
import org.datamate.authz.application.port.in.SavePoliciesUseCase;
import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.dto.policy.SavePoliciesRequest;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PharmacyAuthzService {
    
    // Inject the library's inbound ports
    private final GetConditionFieldsUseCase getConditionFieldsUseCase;
    private final GetNamespacesUseCase getNamespacesUseCase;
    private final GetPoliciesUseCase getPoliciesUseCase;
    private final SavePoliciesUseCase savePoliciesUseCase;

    public List<ConditionFieldDto> getFields(String permissionCode) {
        // custom validations could be added here
        return getConditionFieldsUseCase.getFields(permissionCode);
    }

    public List<String> getNamespaces() {
        // custom validations could be added here
        return getNamespacesUseCase.getNamespaces();
    }

    public List<PolicyGridItemDto> getPolicies(SubjectType subjectType, String subjectId, String namespace) {
        // custom validations could be added here
        return getPoliciesUseCase.getPolicies(subjectType, subjectId, namespace);
    }

    public void savePolicies(SavePoliciesRequest request) {
        // custom validations could be added here
        // e.g., verifying if the subject exists in the Pharmacy database
        
        savePoliciesUseCase.savePolicies(request);
    }
}
```

The microservice's REST Controllers can then inject this domain service instead of calling the library directly, keeping the HTTP layer thin and ignorant of library specifics.

### 3. API Naming Standards

To ensure the Admin UI can seamlessly connect to any microservice managing authorization, all microservices MUST adhere to the following REST API routing standard when exposing these wrapped endpoints:

Base Path: `/internal/authz`

| HTTP Method | Endpoint Path | Description |
| :--- | :--- | :--- |
| `GET` | `/namespaces` | Fetches available namespaces. |
| `GET` | `/permissions/{permissionCode}/fields` | Fetches condition fields for a specific permission. |
| `GET` | `/policies` | Fetches policies (requires `subjectType`, `subjectId`, `namespace` params). |
| `PUT` | `/policies` | Saves/Updates policies via full-state sync. |

Example Controller:
```java
@RestController
@RequestMapping("/internal/authz")
@RequiredArgsConstructor
public class AuthzPolicyController {
    
    private final PharmacyAuthzService pharmacyAuthzService;

    // Implements GET /internal/authz/namespaces
    // Implements GET /internal/authz/permissions/{permissionCode}/fields
    // Implements GET /internal/authz/policies
    // Implements PUT /internal/authz/policies
}
```

### 4. Implementing the Outbound Port in the Consumer

To allow the library to validate subjects, the consumer implements the library's `SubjectValidationPort`.

```java
package com.yourcompany.microservice.adapter.out.authz;

import org.datamate.authz.application.port.out.SubjectValidationPort;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubjectValidationAdapter implements SubjectValidationPort {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public boolean subjectExists(SubjectType type, String subjectId) {
        if (type == SubjectType.USER) {
            return userRepository.existsById(subjectId);
        } else if (type == SubjectType.ROLE) {
            return roleRepository.existsById(subjectId);
        }
        return false;
    }
}
```
