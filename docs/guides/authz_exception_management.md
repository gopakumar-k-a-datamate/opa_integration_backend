# Authorization Framework Exception Management

## Overview
The `authz-core` framework follows a strict, layered exception management design. It ensures that authorization logic remains completely decoupled from specific infrastructural protocols (such as HTTP status codes or Spring MVC annotations) while still providing a seamless mechanism for downstream microservices to map these exceptions into the Datamate Bedrock ecosystem.

## Core Principles
1. **Purity (No Infrastructure Leakage):** 
   The core framework (`authz-core`) is 100% pure Java. It does not import Spring Web, Spring Security (`AccessDeniedException`), or define HTTP status codes on its exceptions.
2. **Rich Contextual Information:** 
   Exceptions hold meaningful contextual data (like `AuthzErrorCode` and error messages) derived from deep engines (like Rego policies via OPA) which can be properly logged and bubbled up to the client.
3. **Fail-Closed Design:** 
   If any exception or unexpected payload occurs during the policy evaluation process, the framework defaults to denying access by throwing the appropriate `AuthzException`.

---

## 1. Exception Architecture in `authz-core`

The core module relies on a unified hierarchy of exceptions inheriting from `org.datamate.authz.exception.AuthzException` (which extends `RuntimeException`).

### 1.1 The Exception Hierarchy
All framework exceptions reside in the `org.datamate.authz.exception` package:
- `AuthzException`: The root exception class containing an `AuthzErrorCode` and a detail message.
  - `AuthzDeniedException`: Thrown when a user explicitly does not have permission (returns false from OPA or denied result).
  - `AuthzInvalidPayloadException`: Thrown when invalid data is provided (e.g., an empty permission code).
  - `AuthzInvalidSyntaxException`: Thrown when an AST or internal compilation process encounters invalid syntax.
  - `PolicyCompilationException`: Thrown during policy compilation workflows.
  - `AuthzStaleDataException`: Thrown if local cache or bundle states are stale.
  - `AuthzEngineConfigurationException`: Thrown when sidecars (like OPA) are misconfigured (e.g., missing URLs).

### 1.2 The `EvaluationResult` Record
To support modern, rich policies (like Rego policies that return a `"reason"` string alongside the boolean `"allow"` flag), the evaluation clients (`RestPolicyEvaluationClient` and `OpaPolicyEvaluationClient`) return an `EvaluationResult`.
```java
public record EvaluationResult(
        boolean allowed,
        String message,
        String errorCode,
        Map<String, Object> metadata
)
```
**Workflow:**
1. The client queries the OPA sidecar.
2. OPA responds with JSON (e.g., `{"result": {"allow": false, "reason": "Insufficient funds"}}`).
3. The client parses this into an `EvaluationResult.denied("Insufficient funds")`.
4. `DefaultPolicyEnforcer` checks `.allowed()`. If false, it extracts the `.message()` and throws `new AuthzDeniedException(result.message())`.

---

## 2. Integration: How Microservices Handle Exceptions

Because `authz-core` throws pure `AuthzException`s, microservices utilizing Datamate Bedrock need a bridge to convert these into HTTP `ProblemDetail` structures.

This is achieved using the **Adapter Pattern** via the `bedrock-authz-starter` module and the microservice's `@RestControllerAdvice`.

### The Microservice Global Exception Handler (Inline Mapping)
Microservices (e.g., `pharmacy-microservice`) simply extend the standard Bedrock `GlobalExceptionHandler` and add `@ExceptionHandler` methods to map specific domain exceptions into HTTP `ProblemDetail` structures (which is the Spring 6 standard).

Instead of relying on a dedicated adapter class in the starter (which tightly couples dependencies), the microservice's REST layer—acting as the "Driving Adapter"—maps the domain exception inline.

```java
// pharmacy-microservice
@RestControllerAdvice
public class PharmacyGlobalExceptionHandler extends GlobalExceptionHandler {

    public PharmacyGlobalExceptionHandler(MessageResolver resolver, ExceptionProperties properties) {
        super(resolver, properties);
    }

    @ExceptionHandler(AuthzException.class)
    public ProblemDetail handleAuthzException(AuthzException ex, HttpServletRequest request) {
        // Adapt the pure AuthzException into the Bedrock flow inline
        BaseAppException bedrockException = new BaseAppException(
                ex.getErrorCode().name(), 
                ex.getMessage(), 
                ex.getCause()
        );
        return handleBase(bedrockException, request);
    }
}
```

> [!TIP]
> **Why do it this way?** 
> By explicitly mapping subclasses like `AuthzDeniedException`, `AuthzInvalidSyntaxException`, and `AuthzInvalidPayloadException`, the microservice can assign highly specific HTTP status codes (e.g. 403 Forbidden vs 400 Bad Request) and ProblemDetail titles that best fit the context, without polluting the core domain with HTTP concerns.

---

## 3. Important Implementation Notes

### Handling Sidecar Communication Failures
If the OPA engine goes offline or responds with an unparseable 500 error, `RestPolicyEvaluationClient` will catch the `RestClientException` and default to a safe state:
```java
return EvaluationResult.denied("Access Denied: You do not have permission to perform this action.");
```
This fail-closed mechanism guarantees that users are denied access rather than failing open due to infrastructural glitches.

### Throwing Application-Specific Errors
When writing Rego policies, you can now return an object that contains detailed reasoning:
```rego
deny[{"reason": msg}] {
    input.permission == "finance:invoice:read"
    input.resource.department != "HR"
    msg := "You can only read invoices belonging to the HR department."
}
```
The client will parse the `reason`, pass it to `EvaluationResult`, throw an `AuthzDeniedException` containing that message, and the microservice's `PharmacyGlobalExceptionHandler` will translate that message exactly into a 403 Forbidden `ProblemDetail` for the UI frontend.

### Mapping Custom Status Codes
Because `AuthzException` has an internal `AuthzErrorCode` enum, you can configure the Datamate Bedrock framework (e.g., via `messages.properties` or Exception configuration beans) to map `AUTHZ_DENIED` to `403 FORBIDDEN` and `AUTHZ_INVALID_PAYLOAD` to `400 BAD REQUEST`, ensuring complete alignment with standard HTTP protocols without hardcoding them into the library.
