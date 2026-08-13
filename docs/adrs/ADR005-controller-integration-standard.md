# Authz-OPA Library Integration Guide

This guide explains how to integrate the `authz-opa` library into your microservices or modular monolith using a strict **Clean Architecture / Hexagonal Architecture** approach.

## 🏗️ Architecture Overview

The `authz-opa` library provides core authorization logic through its **Service Layer**. While it can automatically configure REST controllers using `datamate.authz.admin.enabled=true`, in federated environments it is recommended that the consuming application disables the built-in admin controllers and wraps the library's services directly.

Instead of building dedicated REST controllers just to wrap these services, the consuming application can inject the library's core services (e.g., `PolicyManagementService`) directly into its own domain services (e.g., a `RoleManagementService` or `PharmacyAuthzService`).

### Library Boundaries

*   **Core Services (`*Service`)**: Interfaces defining the capabilities the library offers (e.g., `PolicyManagementService`).
*   **Application Services (`*Service` Implementations)**: The concrete implementations of those interfaces inside the library.
*   **Outbound Ports (`*Port`)**: Interfaces defining what the library needs from external systems or the consuming application (e.g., `PolicyRepositoryPort`, `SubjectValidationPort`).

---

## 💻 Example Integration

### 1. Inside the `authz-opa` Library

The library defines the use case and implements the orchestration and validation within its own Service layer. If it needs to validate something against the consumer's database (like checking if a subject exists), it uses an **Outbound Port**.

```java
// --- Core Service Interface ---
public interface PolicyManagementService {
    void savePolicies(SavePoliciesRequest request);
    // ...
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
public class DefaultPolicyManagementService implements PolicyManagementService {

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
import org.datamate.authz.service.policy.PolicyManagementService;
import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.rest.dto.SavePoliciesRequest;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PharmacyAuthzService {
    
    // Inject the library's consolidated service
    private final PolicyManagementService policyManagementService;

    public List<ConditionFieldDto> getFields(String permissionCode) {
        // custom validations could be added here
        return policyManagementService.getConditionFields(permissionCode);
    }

    public List<String> getNamespaces() {
        // custom validations could be added here
        return policyManagementService.getNamespaces();
    }

    public List<PolicyGridItemDto> getPolicies(SubjectType subjectType, String subjectId, String namespace) {
        // custom validations could be added here
        return policyManagementService.getPolicies(subjectType, subjectId, namespace);
    }

    public void savePolicies(SavePoliciesRequest request) {
        // custom validations could be added here
        // e.g., verifying if the subject exists in the Pharmacy database
        
        policyManagementService.savePolicies(request);
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
