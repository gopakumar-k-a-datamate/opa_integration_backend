# Authz-Core (Authorization Framework)

`authz-core` is a dynamic, decoupled, and engine-agnostic authorization framework designed for enterprise Java/Spring Boot microservices. By default, it integrates seamlessly with **Open Policy Agent (OPA)**, allowing policy administrators to write, compile, and deploy security rules without altering the underlying microservice code.

## Key Features

- **Decoupled Policy Enforcement:** Code dictates *what* is being accessed; external Rego policies dictate *who* can access it.
- **Fail-Closed Architecture:** Highly secure by default. Unannotated or unrecognized resources passed to the enforcer are denied access immediately.
- **Engine Agnostic:** The core `AuthorizationContext` is independent of OPA. The framework ships with an `OpaPolicyEvaluationClient` adapter, but it can be extended to support any policy engine.
- **Dynamic Rego Generation:** The framework includes a compiler that transforms UI-friendly AST (Abstract Syntax Tree) representations of rules into executable Rego code.
- **Attribute-Based Access Control (ABAC):** Evaluate policies not just on user roles, but on the runtime data of the resources being manipulated (via `@PolicyField`).

## Quickstart

### 1. Include the Starter
Add the `bedrock-authz-starter` to your Spring Boot project dependencies.

### 2. Configure Properties
In your `application.yml`:
```yaml
authz:
  opa:
    evaluation-url: "http://localhost:8181/v1/data"
    validation-url: "http://localhost:8181/v1/data"
```

### 3. Annotate REST Endpoints
For simple API-level Role-Based Access Control (RBAC):
```java
@RestController
public class InvoiceController {

    @ProtectedResource("finance:invoice:read")
    @GetMapping("/api/invoices")
    public List<Invoice> getInvoices() {
        return invoiceService.findAll();
    }
}
```

### 4. Annotate Domain Objects (Commands/DTOs)
For Attribute-Based Access Control (ABAC) where policies depend on the payload:
```java
@PolicyResource(namespace = "finance", resourceName = "invoice", action = "create")
public class CreateInvoicePolicyResource {
    
    @PolicyField(displayName = "Department ID", type = FieldType.STRING)
    private String departmentId;

    @PolicyField(displayName = "Invoice Amount", type = FieldType.NUMBER)
    private double amount;
}
```

Then evaluate the command in your service layer before execution:
```java
public void createInvoice(CreateInvoicePolicyResource resource) {
    policyEnforcer.enforce(resource); // Throws AuthzDeniedException if denied
    // proceed...
}
```

## Exception Handling

When the `PolicyEnforcer` (or the `@ProtectedResource` aspect) denies a request, it throws an `AuthzDeniedException`. 
To ensure your API clients receive a proper `403 Forbidden` response instead of a generic `500 Server Error`, you should map this exception in your global `@RestControllerAdvice`:

```java
@RestControllerAdvice
public class GlobalAuthzExceptionHandler {

    @ExceptionHandler(AuthzDeniedException.class)
    public ProblemDetail handleAuthzDenied(AuthzDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pd.setTitle("ACCESS_DENIED");
        return pd;
    }
}
```

## Configuration Properties

The `bedrock-authz-starter` provides several configurable properties in your `application.yml` or `application.properties`:

| Property | Default Value | Description |
|----------|---------------|-------------|
| `authz.opa.evaluation-url` | `http://localhost:8181` | The OPA endpoint used by the enforcer to evaluate policies. Usually points to the data API (e.g. `http://localhost:8181/v1/data`). |
| `authz.opa.validation-url` | `http://localhost:8181` | The OPA endpoint used to validate Rego syntax before saving. |
| `authz.jpa.enabled` | `true` | Auto-configures JPA Entities, Repositories, and Flyway migrations for storing AST policies in your database. Set to `false` if your microservice only acts as a client that evaluates policies without managing them. |

## Documentation

For a deeper understanding of how the framework operates, please refer to the detailed documentation:

- [Architecture Decision Record (ADR 0001)](../docs/adr/0001-opa-policy-enforcement-architecture.md): Why we chose OPA and the Fail-Closed security design.
- [Security Model & Context Flow](../docs/security-model.md): How identity is extracted from Spring Security/Bedrock JWT.
- [Annotations Guide](../docs/annotations.md): Detailed usage of `@ProtectedResource`, `@PolicyResource`, and `@PolicyField`.
- [Rego Compiler & Generation](../docs/rego-generation.md): How dynamic AST policies are compiled into OPA Rego blocks.
