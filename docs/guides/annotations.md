# Annotations Guide

The `authz-core` framework uses a declarative approach to policy enforcement. Rather than writing imperative `if/else` checks for permissions, you decorate your controllers, methods, or domain objects with specific annotations. The framework's `PolicyEnforcer` (often wired via AOP or manual calls) reads these annotations to determine access rights.

## `@ProtectedResource` (For REST Controllers)

The `@ProtectedResource` annotation is designed for API endpoints. It defines a flat permission string that is required to execute the method.

**Usage:**
```java
@RestController
@RequestMapping("/pharmacy")
public class PrescriptionController {

    @ProtectedResource("pharmacy:prescription:create")
    @PostMapping("/prescription")
    public String createPrescription(@RequestBody CreatePrescriptionRequest body) {
        // Business logic...
        return "Success";
    }

    @ProtectedResource("pharmacy:prescription:read")
    @GetMapping("/prescription")
    public String readPrescription() {
        return "List of prescriptions";
    }
}
```

**How it works:**
When a request hits an endpoint annotated with `@ProtectedResource`, the framework evaluates the specified permission string (e.g., `pharmacy:prescription:create`) against the user's roles. If the policy engine (OPA) denies the request, an `AuthzDeniedException` is thrown before the method executes.

> [!WARNING]
> **RBAC vs ABAC Conflict:** 
> The `@ProtectedResource` annotation evaluates pure **RBAC** (Role-Based Access Control) and sends an empty resource context (`{}`) to OPA. 
> 
> If your Rego policy requires fine-grained attributes (ABAC) for a specific action (e.g., `input.resource.drugCategory == "category"`), **do not use `@ProtectedResource`** on the controller. The aspect will intercept the request and immediately deny it because the attributes are missing. Instead, remove the annotation from the controller and explicitly call `policyEnforcer.enforce(payload)` inside your service layer using an object annotated with `@PolicyResource` and `@PolicyField`.

---

## `@PolicyResource` (For Domain Objects & Commands)

While `@ProtectedResource` is great for simple role-based access control at the API edge, true attribute-based access control (ABAC) requires evaluating the *data* being manipulated.

The `@PolicyResource` annotation is placed on classes (such as Commands or DTOs) to define the namespace, resource name, and action taking place.

**Usage:**
```java
@PolicyResource(namespace = "finance", resourceName = "journal", action = "create")
public class CreateJournalPolicyResource {
    private String title;
    private double amount;
    
    // Getters and Setters
}
```

When this policy resource is passed to the enforcer:
```java
public void execute(CreateJournalPolicyResource resource) {
    policyEnforcer.enforce(command); // Will throw AuthzDeniedException if unauthorized
    // Proceed with creation
}
```
The enforcer dynamically builds the permission string `finance:journal:create`. 

> [!WARNING]
> **Fail-Closed Architecture:** If you pass an object to `policyEnforcer.enforce(Object)` that does *not* have the `@PolicyResource` annotation, the framework will **deny access** by default. Ensure all protected domain objects are properly annotated.

---

## `@PolicyField` (For Attribute-Based Access Control)

To evaluate rules based on the contents of the resource (e.g., "A doctor can only read prescriptions assigned to their own clinic"), you use `@PolicyField`.

This annotation tells the `PolicyEnforcer` to extract the value of the field and send it to OPA inside the `resource` map.

**Usage:**
```java
@PolicyResource(namespace = "pharmacy", resourceName = "prescription", action = "read")
public class ReadPrescriptionQuery {
    
    @PolicyField
    private String clinicId;
    
    @PolicyField
    private double cost;
    
    // ...
}
```

**OPA Payload:**
When the enforcer processes this query, it constructs an OPA payload that looks like this:
```json
{
  "input": {
    "user": {
      "id": "user-123",
      "roles": ["DOCTOR"]
    },
    "permission": "pharmacy:prescription:read",
    "resource": {
      "clinicId": "clinic-456",
      "cost": 150.50
    }
  }
}
```
Your OPA Rego policy can then evaluate the attributes:
```rego
allow if {
    "DOCTOR" in input.user.roles
    input.resource.clinicId == input.user.clinicId # Example attribute check
}
```
