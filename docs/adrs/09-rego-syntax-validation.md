# Rego Syntax Validation via OPA REST API — Design Document

This document describes how to validate custom Rego snippets entered by admins before saving them to the database and compiling them into OPA bundles.

Reference: [07-enhanced-condition-builder-and-custom-rego.md](./07-enhanced-condition-builder-and-custom-rego.md)

---

## Problem Statement

Administrators will enter raw Rego code via the Admin UI. Before saving to PostgreSQL and compiling into the OPA bundle, the system must verify the Rego syntax is valid. A syntax error in custom Rego corrupts the entire namespace bundle and can cause authorization outages.

Since OPA is written in Go, Java has no native Rego parser. The approach is to delegate validation to OPA itself via its HTTP REST API.

---

## Why OPA REST API

| Approach | Verdict |
|---|---|
| JNI/JNA (Go → C → JVM) | ❌ JVM crash risk from Go panics, goroutine/GC conflicts, cross-compilation maintenance |
| Java Rego parser | ❌ Doesn't exist. Writing one = maintaining compatibility with every OPA release |
| `opa check` CLI | ⚠️ Works but requires binary in container, OS process overhead, platform-specific |
| **OPA REST API** | ✅ OPA already runs as sidecar. Zero new dependencies. Always in sync with OPA version. |

---

## How OPA's Policy API Works

### Upload (Validate + Load)

```
PUT /v1/policies/{id}
Content-Type: text/plain

<rego module text>
```

- **200 OK** → Rego is syntactically valid. Policy is loaded into OPA's active set.
- **400 Bad Request** → Syntax error. Response body contains exact error location.

### Error Response Format (400)

```json
{
  "code": "invalid_parameter",
  "message": "error(s) occurred while compiling module(s)",
  "errors": [
    {
      "code": "rego_parse_error",
      "message": "unexpected token",
      "location": {
        "file": "temp-abc123",
        "row": 3,
        "col": 15
      }
    }
  ]
}
```

### Cleanup

```
DELETE /v1/policies/{id}
```

Removes the temporary policy from OPA's active set.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        authz-core (Domain)                       │
│                                                                   │
│  ┌──────────────────────┐    ┌───────────────────────────────┐   │
│  │ RegoValidationResult │    │ PolicyValidationPort          │   │
│  │  - valid: boolean    │    │  + validate(rego): Result     │   │
│  │  - errors: List      │    │                               │   │
│  └──────────────────────┘    └───────────────────────────────┘   │
│                                         ▲                         │
│  ┌──────────────────────┐               │ implements              │
│  │ RegoValidationError  │               │                         │
│  │  - line: int         │    ┌──────────┴────────────────────┐   │
│  │  - column: int       │    │ OpaPolicyValidationAdapter    │   │
│  │  - message: String   │    │  (authz-opa module)           │   │
│  └──────────────────────┘    │  - uses RestTemplate/WebClient│   │
│                               │  - PUT → validate             │   │
│                               │  - DELETE → cleanup           │   │
│                               └──────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Implementation

### Layer 1: Domain Models (authz-core)

#### [NEW] `RegoValidationResult.java`

```java
package org.datamate.authz.domain.model.policy.valueobject;

import java.util.List;

/**
 * Immutable result of a Rego syntax validation.
 */
public record RegoValidationResult(
        boolean valid,
        List<RegoValidationError> errors
) {
    public static RegoValidationResult success() {
        return new RegoValidationResult(true, List.of());
    }

    public static RegoValidationResult failure(List<RegoValidationError> errors) {
        return new RegoValidationResult(false, errors);
    }
}
```

#### [NEW] `RegoValidationError.java`

```java
package org.datamate.authz.domain.model.policy.valueobject;

/**
 * A single syntax error in a Rego snippet, with exact location for
 * the UI code editor to highlight.
 */
public record RegoValidationError(
        int line,
        int column,
        String message
) {}
```

---

### Layer 2: Outbound Port (authz-core)

#### [NEW] `PolicyValidationPort.java`

```java
package org.datamate.authz.api.policy;

import org.datamate.authz.domain.model.policy.valueobject.RegoValidationResult;

/**
 * Port for validating Rego syntax. The implementation delegates to
 * an external OPA instance via its REST API.
 */
public interface PolicyValidationPort {

    /**
     * Validates a raw Rego snippet for syntax correctness.
     *
     * <p>The implementation wraps the snippet in a temporary package,
     * sends it to OPA for parsing, and cleans up immediately after.</p>
     *
     * @param regoSnippet The raw Rego text (rule blocks only, no package/import).
     * @return Validation result with error locations if invalid.
     */
    RegoValidationResult validate(String regoSnippet);
}
```

---

### Layer 3: Infrastructure Adapter (authz-opa)

#### [NEW] `OpaPolicyValidationAdapter.java`

```java
package org.datamate.authz.adapter.out.opa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.datamate.authz.api.policy.PolicyValidationPort;
import org.datamate.authz.domain.model.policy.valueobject.RegoValidationError;
import org.datamate.authz.domain.model.policy.valueobject.RegoValidationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class OpaPolicyValidationAdapter implements PolicyValidationPort {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String opaPolicyBaseUrl;

    // Lines added by wrapper (package + import + blank line)
    // Used to adjust error line numbers back to the snippet's frame
    private static final int WRAPPER_LINE_OFFSET = 4;

    public OpaPolicyValidationAdapter(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${authz.opa.validation.url:http://localhost:8181}") String opaBaseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.opaPolicyBaseUrl = opaBaseUrl + "/v1/policies/";
    }

    @Override
    public RegoValidationResult validate(String regoSnippet) {
        // 1. Generate unique policy ID to prevent collisions
        String policyId = "_validation_" + UUID.randomUUID().toString().replace("-", "");

        // 2. Wrap snippet in a temporary package
        //    This package name will never match real queries (app.authz.*)
        String fullModule = buildTemporaryModule(policyId, regoSnippet);

        // 3. Send to OPA
        String url = opaPolicyBaseUrl + policyId;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            HttpEntity<String> request = new HttpEntity<>(fullModule, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return RegoValidationResult.success();
            }

            // Unexpected non-2xx, non-4xx
            return RegoValidationResult.failure(List.of(
                    new RegoValidationError(0, 0, "Unexpected OPA response: " + response.getStatusCode())
            ));

        } catch (HttpClientErrorException.BadRequest e) {
            // 4. Parse OPA's error response
            return parseOpaErrors(e.getResponseBodyAsString());

        } catch (RestClientException e) {
            // 5. OPA is unreachable — fail open for validation (admin can still save)
            //    OR fail closed (block save) — depends on your policy
            log.error("OPA validation endpoint unreachable at {}", url, e);
            return RegoValidationResult.failure(List.of(
                    new RegoValidationError(0, 0,
                            "Rego validation service unavailable. Cannot verify syntax.")
            ));

        } finally {
            // 6. Cleanup — remove temporary policy from OPA
            cleanupTemporaryPolicy(url);
        }
    }

    /**
     * Wraps the raw snippet in a complete Rego module with a unique
     * package name that will never match real authorization queries.
     */
    private String buildTemporaryModule(String policyId, String snippet) {
        return "package " + policyId + "\n"
                + "\n"
                + "import rego.v1\n"
                + "\n"
                + snippet;
    }

    /**
     * Parses OPA's 400 error response into domain error objects.
     * Adjusts line numbers by subtracting the wrapper offset so errors
     * map back to the admin's original snippet lines.
     *
     * OPA error format:
     * {
     *   "code": "invalid_parameter",
     *   "message": "error(s) occurred while compiling module(s)",
     *   "errors": [
     *     {
     *       "code": "rego_parse_error",
     *       "message": "unexpected token",
     *       "location": { "file": "...", "row": 7, "col": 15 }
     *     }
     *   ]
     * }
     */
    private RegoValidationResult parseOpaErrors(String responseBody) {
        List<RegoValidationError> errors = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode errorsNode = root.get("errors");

            if (errorsNode != null && errorsNode.isArray()) {
                for (JsonNode errorNode : errorsNode) {
                    String message = errorNode.has("message")
                            ? errorNode.get("message").asText()
                            : "Unknown syntax error";

                    int line = 0;
                    int column = 0;
                    JsonNode location = errorNode.get("location");
                    if (location != null) {
                        line = Math.max(0, location.path("row").asInt(0) - WRAPPER_LINE_OFFSET);
                        column = location.path("col").asInt(0);
                    }

                    errors.add(new RegoValidationError(line, column, message));
                }
            }

            if (errors.isEmpty()) {
                // Fallback: use the top-level message
                String message = root.has("message")
                        ? root.get("message").asText()
                        : "Rego syntax error";
                errors.add(new RegoValidationError(0, 0, message));
            }

        } catch (Exception e) {
            log.warn("Failed to parse OPA error response: {}", responseBody, e);
            errors.add(new RegoValidationError(0, 0, "Rego syntax error (unparseable details)"));
        }

        return RegoValidationResult.failure(errors);
    }

    /**
     * Removes the temporary policy from OPA's active set.
     * Fire-and-forget with error logging — cleanup failure is non-fatal
     * because the temp package name never matches real queries.
     */
    private void cleanupTemporaryPolicy(String url) {
        try {
            restTemplate.delete(url);
        } catch (RestClientException e) {
            log.warn("Failed to cleanup temporary OPA policy at {}. "
                    + "Policy uses unreachable package name, so this is non-fatal.", url, e);
        }
    }
}
```

---

### Layer 4: Domain Exception (authz-core)

#### [NEW] `InvalidPolicySyntaxException.java`

```java
package org.datamate.authz.exception;

import org.datamate.authz.domain.model.policy.valueobject.RegoValidationError;

import java.util.List;

/**
 * Thrown when a custom Rego snippet fails syntax validation.
 * Contains structured error information for the UI code editor.
 */
public class InvalidPolicySyntaxException extends RuntimeException {

    private final List<RegoValidationError> errors;

    public InvalidPolicySyntaxException(List<RegoValidationError> errors) {
        super("Custom Rego snippet has " + errors.size() + " syntax error(s)");
        this.errors = errors;
    }

    public List<RegoValidationError> getErrors() {
        return errors;
    }
}
```

---

### Layer 5: Application Service Integration (authz-core)

#### [MODIFY] `SavePoliciesService.java`

```java
@RequiredArgsConstructor
@Service
public class SavePoliciesService implements SavePolicies {

    private final PolicyValidationPort validationPort;  // NEW
    // ... existing fields ...

    @Override
    @Transactional
    public void execute(SavePoliciesRequest request) {
        for (PolicyItemRequest item : request.policies()) {

            // ─── VALIDATE BEFORE TRANSACTION WORK ───
            if (item.useCustomRego()) {
                validateCustomRego(item);
            }

            // ─── EXISTING SAVE LOGIC (unchanged) ───
            // ...
        }
    }

    private void validateCustomRego(PolicyItemRequest item) {
        // 1. Basic validations (empty, max length, rule names, permission code)
        if (item.customRegoSnippet() == null || item.customRegoSnippet().isBlank()) {
            throw new InvalidPayloadException("Custom Rego snippet cannot be empty.");
        }
        if (item.customRegoSnippet().length() > 10_000) {
            throw new InvalidPayloadException("Custom Rego snippet exceeds maximum length.");
        }
        validateSnippetRuleNames(item.customRegoSnippet());
        validatePermissionInSnippet(item.customRegoSnippet(), item.permissionCode());

        // 2. Rego syntax validation via OPA
        RegoValidationResult result = validationPort.validate(item.customRegoSnippet());
        if (!result.valid()) {
            throw new InvalidPolicySyntaxException(result.errors());
        }
    }
}
```

> **Note:** The `validationPort.validate()` call happens INSIDE `@Transactional` in this example, but since it's an HTTP call (not DB I/O), the DB connection is not blocked during the HTTP round-trip — Spring's connection is lazily acquired on first actual DB operation. If your connection pool eagerly acquires, consider moving validation to a separate pre-validation step before `@Transactional`.

---

### Layer 6: REST Error Handling (authz-rest)

#### [MODIFY] `GlobalExceptionHandler.java` (or equivalent)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPolicySyntaxException.class)
    public ResponseEntity<Map<String, Object>> handleRegoSyntaxError(
            InvalidPolicySyntaxException ex) {

        List<Map<String, Object>> errorDetails = ex.getErrors().stream()
                .map(e -> Map.<String, Object>of(
                        "line", e.line(),
                        "column", e.column(),
                        "message", e.message()
                ))
                .toList();

        Map<String, Object> body = Map.of(
                "status", 400,
                "error", "REGO_SYNTAX_ERROR",
                "message", ex.getMessage(),
                "details", errorDetails
        );

        return ResponseEntity.badRequest().body(body);
    }
}
```

**API Response:**

```json
{
  "status": 400,
  "error": "REGO_SYNTAX_ERROR",
  "message": "Custom Rego snippet has 1 syntax error(s)",
  "details": [
    {
      "line": 3,
      "column": 15,
      "message": "unexpected token: expected \"}\""
    }
  ]
}
```

The frontend code editor (Monaco/CodeMirror) can use `line` and `column` to place red squiggly underlines exactly where the error is.

---

### Layer 7: Configuration

#### `application.yml`

```yaml
authz:
  opa:
    validation:
      # Local dev — use the existing OPA sidecar
      url: http://localhost:8181
      
      # Production — use dedicated validation OPA in control plane
      # url: http://opa-validator.authz-control-plane.svc.cluster.local:8181
```

---

## Validation Flow Diagram

```
Admin types Rego in UI code editor
│
├── Clicks "Save"
│   │
│   ├── REST Controller receives request
│   │
│   ├── SavePoliciesService.validateCustomRego()
│   │   │
│   │   ├── Basic checks (empty, length, rule names, permission code)
│   │   │
│   │   ├── validationPort.validate(snippet)
│   │   │   │
│   │   │   ├── Generate UUID: _validation_a1b2c3d4...
│   │   │   │
│   │   │   ├── Wrap in temp module:
│   │   │   │   package _validation_a1b2c3d4...
│   │   │   │   import rego.v1
│   │   │   │   <snippet>
│   │   │   │
│   │   │   ├── PUT /v1/policies/_validation_a1b2c3d4...
│   │   │   │   │
│   │   │   │   ├── 200 OK → syntax valid ✅
│   │   │   │   └── 400 Bad Request → parse errors → extract line/col
│   │   │   │
│   │   │   └── finally: DELETE /v1/policies/_validation_a1b2c3d4...
│   │   │
│   │   ├── ✅ Valid → proceed to DB save
│   │   └── ❌ Invalid → throw InvalidPolicySyntaxException
│   │
│   ├── ✅ DB save → trigger recompile → bundle updated
│   │
│   └── ❌ 400 response with line/column errors → UI highlights errors
```

---

## Production Deployment: Dedicated Validation OPA

For production, use a separate OPA container for validation to avoid:
- Temporary policies (even with unreachable package names) existing in the runtime OPA
- Validation HTTP calls competing with live authorization checks

```
┌─────────────────────────────────────────────────────────┐
│                  Kubernetes Cluster                       │
│                                                           │
│  ┌───────────────────┐     ┌──────────────────────────┐  │
│  │  Control Plane     │     │  Data Plane (per service) │  │
│  │                    │     │                            │  │
│  │  Admin UI ──────┐  │     │  App Pod                   │  │
│  │                 │  │     │  ┌───────┐  ┌───────────┐  │  │
│  │  Admin API ─────┤  │     │  │ App   │──│ OPA       │  │  │
│  │                 │  │     │  │       │  │ Sidecar   │  │  │
│  │  OPA Validator ◄┘  │     │  │       │  │ (runtime) │  │  │
│  │  (validation only) │     │  └───────┘  └───────────┘  │  │
│  │  - no bundles      │     │                            │  │
│  │  - no runtime eval │     │  Bundles flow:             │  │
│  │  - lightweight     │     │  DB cache → OPA sidecar    │  │
│  └───────────────────┘     └──────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

The validation OPA container:
- Runs with minimal configuration (no bundles, no decision logs)
- Only accepts PUT/DELETE on `/v1/policies`
- Is stateless — can be restarted without impact
- Scales independently from runtime OPA sidecars

---

## File Summary

| Layer | File | Module | Status |
|---|---|---|---|
| Domain Model | `RegoValidationResult.java` | authz-core | NEW |
| Domain Model | `RegoValidationError.java` | authz-core | NEW |
| Port | `PolicyValidationPort.java` | authz-core | NEW |
| Exception | `InvalidPolicySyntaxException.java` | authz-core | NEW |
| Adapter | `OpaPolicyValidationAdapter.java` | authz-opa | NEW |
| Service | `SavePoliciesService.java` | authz-core | MODIFY |
| REST | `GlobalExceptionHandler.java` | authz-rest | MODIFY |
| Config | `application.yml` | per-service | MODIFY |
