# ADR-001: Dual-Integration Architecture for Authz Endpoints

**Status:** Accepted  
**Date:** 2026-08-18

## Context

The Bedrock Authz library provides core policy management functionality via `PolicyManagementService`. However, every consumer application (Pharmacy, Lab, Billing, etc.) needs to expose these operations over HTTP to their respective frontends. 

Historically, this required every consumer team to write their own REST controllers, wire up the service, and handle HTTP responses manually. This led to:
1. **High boilerplate** for consumers.
2. **Inconsistent API contracts** across microservices.
3. **Duplicated authorization logic** in controllers.

We needed a way to standardize the API contract across the organization while still allowing consumers to maintain absolute control over *who* is authorized to call those endpoints and the ability to inject custom business logic when necessary.

## Decision

We have adopted a **Dual-Integration Architecture**. The library now exposes two API surfaces, allowing consumers to choose the right abstraction level for each individual endpoint.

1. **Approach 1: Programmatic API** (Consumer builds the controller)
2. **Approach 2: Bean-Activated Endpoints** (Library provides dormant controllers, activated by consumer beans)

Consumers can mix and match these approaches within the same application.

---

## Approach 1: Programmatic API

The consumer injects `PolicyManagementService` into their own custom REST controller.

**When to use:**
- You need custom URL paths or DTOs.
- You need consumer-specific business logic (e.g., custom validation, auditing, or triggering side-effects before/after saving).

### Code Example

```java
@RestController
@RequestMapping("/api/pharmacy/authz")
public class PharmacySavePoliciesController {

    private final PolicyManagementService policyService; // Library Service

    public PharmacySavePoliciesController(PolicyManagementService policyService) {
        this.policyService = policyService;
    }

    @PutMapping("/policies")
    public ResponseEntity<Void> savePolicies(@RequestBody SavePoliciesRequest request) {
        
        // 1. Consumer-specific logic BEFORE
        if (request.getPolicies().size() > 100) {
            throw new IllegalArgumentException("Batch limit exceeded");
        }

        // 2. Library logic
        policyService.savePolicies(request);

        // 3. Consumer-specific logic AFTER
        notifyComplianceTeam(request);

        return ResponseEntity.ok().build();
    }
}
```

---

## Approach 2: Bean-Activated Endpoints

The library ships with pre-built REST controllers (e.g., `FieldsController`, `PoliciesController`) that are **dormant by default**. 

The consumer activates them by providing a named `EndpointAuthorization` bean. The library controllers use `@ConditionalOnBean(name = ...)` to detect the consumer's intent. If the bean exists, the endpoint is exposed. If not, the endpoint simply does not exist.

**When to use:**
- You want the standard API contract.
- You have no custom business logic for the endpoint.
- You want minimal boilerplate (zero controllers to write).

### Scenario A: The Simplest Case (Same Auth for All Endpoints)

If your application applies the same authorization rule (e.g., "Must have MANAGE_POLICIES permission") to all endpoints, you can share a single lambda across multiple beans.

```java
@Configuration
public class PharmacyAuthzConfig {

    // Define the common rule once
    private final EndpointAuthorization commonAuth = context -> {
        if (!currentUserHas("MANAGE_POLICIES")) {
            throw new AccessDeniedException("Not authorized");
        }
    };

    // Activate the endpoints you want by returning the rule
    @Bean(AuthzBeans.FIELDS)
    public EndpointAuthorization fieldsAuth()       { return commonAuth; }

    @Bean(AuthzBeans.POLICIES)
    public EndpointAuthorization policiesAuth()     { return commonAuth; }

    @Bean(AuthzBeans.BUNDLE)
    public EndpointAuthorization bundleAuth()       { return commonAuth; }

    // Endpoints for NAMESPACES and SAVE_POLICIES are NOT activated
}
```

### Scenario B: Fine-Grained Authorization

If different endpoints require different rules, you can define specific logic for each bean. The `AuthorizationContext` is a sealed interface that provides type-safe access to request parameters.

```java
@Configuration
public class PharmacyAuthzConfig {

    @Bean(AuthzBeans.FIELDS)
    public EndpointAuthorization fieldsAuth() {
        return context -> {
            // Read-only access — just requires authentication
            if (!isAuthenticated()) {
                throw new AccessDeniedException("Login required");
            }
        };
    }

    @Bean(AuthzBeans.SAVE_POLICIES)
    public EndpointAuthorization saveAuth() {
        return context -> {
            // Write access — requires specific role
            if (!currentUserHas("MANAGE_POLICIES_WRITE")) {
                throw new AccessDeniedException("Write access required");
            }
        };
    }

    @Bean(AuthzBeans.BUNDLE)
    public EndpointAuthorization bundleAuth() {
        return context -> {
            // Type-safe context inspection via pattern matching
            if (context instanceof BundleAuthContext ctx) {
                if (!allowedNamespaces.contains(ctx.namespace())) {
                    throw new AccessDeniedException("Namespace not allowed");
                }
            }
        };
    }
}
```

### Scenario C: Mixing Approaches

Consumers can use library controllers for simple read operations while building a custom controller for writes.

```java
@Configuration
public class PharmacyAuthzConfig {

    // 1. Activate standard read endpoints (Approach 2)
    @Bean(AuthzBeans.FIELDS)
    public EndpointAuthorization fieldsAuth() {
        return ctx -> requirePermission("READ_POLICIES");
    }

    @Bean(AuthzBeans.POLICIES)
    public EndpointAuthorization policiesAuth() {
        return ctx -> requirePermission("READ_POLICIES");
    }

    // Notice we do NOT provide AuthzBeans.SAVE_POLICIES
}

// 2. Build custom write endpoint (Approach 1)
@RestController
public class CustomSaveController {
    
    private final PolicyManagementService policyService;
    
    // Custom validation logic here...
    @PutMapping("/api/pharmacy/authz/policies")
    public void savePolicies(...) {
        // ... custom logic ...
        policyService.savePolicies(request);
    }
}
```

## Consequences

**Positive:**
- **Zero Dead Endpoints:** Because we use `@ConditionalOnBean`, unactivated endpoints are never registered with Spring MVC. There are no dangling routes returning 404s.
- **Enforced Security:** Structurally impossible for a consumer to activate a library endpoint without actively providing an authorization lambda.
- **Consistency:** Consumers are heavily incentivized to use the standard API contract for CRUD operations because it requires zero boilerplate.

**Negative:**
- **Magic Strings:** Consumers must remember to use the constants from the `AuthzBeans` class when defining their `@Bean` names.
- **Spring Knowledge Required:** Consumers must understand how `@ConditionalOnBean` and `@Bean` names interact.
