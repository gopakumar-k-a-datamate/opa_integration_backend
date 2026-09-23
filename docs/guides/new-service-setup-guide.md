# New Service Setup Guide

## Context & Purpose
Our authorization framework is decentralized. When establishing a new microservice or modulith, the service is entirely responsible for hosting its own authorization database tables, bundle compilation cache, and OPA sidecar. 

This guide serves as a checklist and baseline reference for the foundational setup required when integrating the authorization framework into a new service.

---

## 1. Project Dependencies

To leverage the core authorization logic, the programmatic `PolicyEnforcer`, and database adapters, you must include the bedrock authorization starter in your project's build file.

**`pom.xml`:**
```xml
<dependency>
    <groupId>org.datamate</groupId>
    <artifactId>bedrock-authz-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## 2. Database & Flyway Configuration

Because we follow a **Database-First** paradigm, your new service must physically own and manage its authorization schema. However, you do **not** need to manually manage the base table structures!

1. **Automatic Base Tables:** The `bedrock-authz-starter` library contains an internal Flyway instance that automatically runs on application startup. It provisions all required authz tables (e.g., `authz_policy`, `authz_resource`) into a dedicated schema without interfering with your application's default Flyway execution.
2. **Populate Data (Optional):** If you wish to seed default resources, permissions, or policies using Flyway, you can create a standard migration script (e.g., `V2__insert_domain_resources.sql`) in your application. Because the library's Flyway runs first, you can safely reference the authz tables (using the schema prefix you configure below).

### How Schema Isolation Works

When you set `database.schema` in your `opa-config.yaml` (e.g., `schema: pharmacy`), the library uses **two independent mechanisms** to isolate authz tables from your application's tables:

#### Dual Flyway Instances

The library creates its **own** Flyway instance completely separate from your application's Spring-managed Flyway. The two never interfere with each other:

| | Library Flyway (authz) | Application Flyway (yours) |
|---|---|---|
| **Migration location** | `classpath:db/authz-migration` (bundled in the library JAR) | Your app's location (e.g., `classpath:db/migration`) |
| **History table** | `authz_flyway_schema_history` | `flyway_schema_history` (Spring Boot default) |
| **Target schema** | From `opa-config.yaml` → `database.schema` | From `spring.flyway.schemas` or the database default |
| **Runs when** | `@PostConstruct` — **before** Spring's Flyway auto-configuration | Spring Boot Flyway auto-config — **after** the library's |
| **Creates schema** | Yes (auto-creates the target schema if it doesn't exist) | Depends on your `spring.flyway.*` settings |

Because the library's Flyway only processes SQL files from `classpath:db/authz-migration`, it will **never** touch or run your application's migration scripts, and vice versa.

#### Hibernate Schema Redirection (`AuthzSchemaIntegrator`)

The library's JPA entities (e.g., `@Table(name = "authz_policy")`) do not hardcode a schema in their annotations. Instead, the library registers a **Hibernate Integrator** that intercepts Hibernate's boot process and rewrites the schema for authz tables only.

This works via a **hardcoded whitelist** of known authz table names:

```
authz_resource, authz_permission, authz_condition_field,
authz_policy, authz_policy_bundle_cache, authz_subject,
authz_resource_audit, authz_permission_audit, authz_policy_audit
```

During Hibernate startup, the integrator iterates **all** mapped tables and calls `table.setSchema(targetSchema)` **only** for tables whose name matches this whitelist. Every other table — from your application, your modules, or any other library — is left completely untouched in whatever schema it was originally mapped to.

#### Resulting Database Layout

```
┌──────────────────────────────────────────────────┐
│                 Same Database                    │
│                                                  │
│  ┌─ "public" schema ──────────────────────┐      │
│  │  your_table_a, your_table_b, ...       │      │  ← Your app/module tables (UNTOUCHED)
│  │  flyway_schema_history                 │      │
│  └────────────────────────────────────────┘      │
│                                                  │
│  ┌─ "my_app_authz" schema ───────────────┐      │
│  │  authz_policy, authz_resource, ...     │      │  ← Only these 9 tables are moved here
│  │  authz_flyway_schema_history           │      │
│  └────────────────────────────────────────┘      │
└──────────────────────────────────────────────────┘
```

> [!IMPORTANT]
> **Safe for Modular Monoliths:** If your consumer application is a modular monolith where multiple modules share a single database and `SessionFactory`, setting `database.schema` will **only** affect the 9 whitelisted authz tables. All other module tables (e.g., `drugs`, `prescriptions`, `orders`, `accounts`) remain in their original schema. The whitelist-based approach guarantees there is no cross-contamination.

> [!NOTE]
> If `database.schema` is set to `"public"` or omitted entirely, both mechanisms become no-ops — the authz tables are created in the default `public` schema alongside your application tables, and the Hibernate integrator is not registered.

---

## 3. OPA Sidecar Configuration (`opa-config.yaml`)

You must create an `opa-config.yaml` file at the root of your project. This single file is used both by the OPA container to configure bundle polling, and by the Java application (`RestPolicyEvaluationClient`) to locate the sidecar.

```yaml
# 1. OPA Agent Configuration
services:
  my_app_api:
    # URL back to your Java Application
    url: http://host.docker.internal:8080

bundles:
  my_app_bundle:
    service: my_app_api
    # Endpoint exposed by bedrock-authz-starter to serve the Rego bundle
    resource: /internal/authz/bundle/<your_namespace>
    polling:
      min_delay_seconds: 10
      max_delay_seconds: 20

default_authorization_decision: /app/authz/<your_namespace>/allow

# 2. Java Application Configuration
database:
  # The schema where the library will automatically provision all authz_* tables.
  # Only the 9 known authz_* tables are placed here; your application/module tables
  # are never affected. Safe for modular monoliths.
  # See "How Schema Isolation Works" in Section 2 for details.
  schema: my_app_authz   # defaults to "public" if omitted

# The endpoint the application will POST to for policy evaluation
evaluation_url: http://localhost:8181/v1/data/app/authz/<your_namespace>/allow
```
*(Replace `<your_namespace>`, the schema name, and the `8080` port to match your specific service).*

---

## 4. Docker Compose Setup (The Sidecar Pattern)

For local development and eventual deployment, the Open Policy Agent must be orchestrated as a sidecar running alongside your application. Update your `docker-compose.yml` to include the OPA image and mount your configuration file.

```yaml
  opa:
    image: openpolicyagent/opa:latest
    container_name: <your_namespace>_opa
    command:
      - "run"
      - "--server"
      - "--addr=0.0.0.0:8181"
      - "--config-file=/config/opa-config.yaml"
    volumes:
      - ./opa-config.yaml:/config/opa-config.yaml:ro
    ports:
      - "8181:8181"
    extra_hosts:
      - "host.docker.internal:host-gateway" # Enables routing back to the host machine
```

---

## 5. Application Code & Enforcement

Finally, link your application's domain commands to the database schema by annotating them, and explicitly trigger enforcement in your application services.

### 1. Annotate the Command
```java
@PolicyResource(namespace = "<your_namespace>", resourceName = "my_resource", action = "read")
public record MyDomainCommand(
    
    @PolicyField(displayName = "Context Field", type = FieldType.STRING)
    String someField

) {}
```

### 2. Enforce Programmatically

Inject the `PolicyEnforcer` into your application service and call `enforce()` before executing business logic.

```java
@Service
@RequiredArgsConstructor
public class MyDomainService {

    private final PolicyEnforcer policyEnforcer;

    public void processCommand(MyDomainCommand command) {
        // Throws AccessDeniedException (HTTP 403) if OPA denies access
        policyEnforcer.enforce(command);
        
        // ... proceed with business logic
    }
}
```

Remember: The `@PolicyResource` and `@PolicyField` annotations act **purely as runtime markers** to extract data for the OPA evaluation payload. They do not auto-register anything in the database!

---

## 6. Activate Authz Management Endpoints

The library ships pre-built REST controllers for managing policies, condition fields, namespaces, subjects, and serving OPA bundles. These controllers follow a **Bean-Activated** pattern — most are **dormant by default** and only become active when you provide a named `EndpointAuthorization` bean.

This ensures it is structurally impossible to expose a management endpoint without explicitly providing an authorization rule for it.

### Endpoint Reference

| Endpoint | Method | Bean Constant | Auto-Configured? | Purpose |
|---|---|---|---|---|
| `/internal/authz/bundle/{namespace}` | `GET` | `AuthzBeans.BUNDLE` | ✅ Yes | Serve compiled OPA bundle to sidecar |
| `/internal/authz/subjects` | `GET` | `AuthzBeans.SUBJECTS` | ✅ Yes | List, search, & paginate subjects (users/roles) |
| `/internal/authz/fields/{permissionCode}` | `GET` | `AuthzBeans.FIELDS` | ❌ No | Get condition fields for a permission |
| `/internal/authz/policies` | `GET` | `AuthzBeans.POLICIES` | ❌ No | Retrieve, search, & paginate policies by subject + namespace |
| `/internal/authz/policies` | `PUT` | `AuthzBeans.SAVE_POLICIES` | ❌ No | Create or update policies |
| `/internal/authz/namespaces` | `GET` | `AuthzBeans.NAMESPACES` | ❌ No | List available namespaces |

> **Note on Pagination & Search:** For details on pagination parameters (`page`, `size`) and search filtering on `/internal/authz/policies` and `/internal/authz/subjects`, see [OPA Pagination & Search Guide](opa-pagination-and-search-guide.md).

> **Auto-configured endpoints** work out of the box with open access (or API-key protection for Bundle). If you define your own `@Bean` with the same name, the auto-configured bean backs off and yours takes over.
>
> **Consumer-activated endpoints** will not exist at runtime unless you define the corresponding bean. There will be no dangling 404 routes — the controller is simply never registered with Spring MVC.

### Scenario A: Shared Authorization Rule

If all endpoints share the same authorization logic (e.g., "must have MANAGE_POLICIES permission"), define a single lambda and reuse it:

```java
@Configuration
public class AuthzEndpointConfig {

    private final EndpointAuthorization commonAuth = context -> {
        if (!currentUserHas("MANAGE_POLICIES")) {
            throw new AccessDeniedException("Not authorized");
        }
    };

    @Bean(AuthzBeans.FIELDS)
    public EndpointAuthorization fieldsAuth()       { return commonAuth; }

    @Bean(AuthzBeans.POLICIES)
    public EndpointAuthorization policiesAuth()      { return commonAuth; }

    @Bean(AuthzBeans.SAVE_POLICIES)
    public EndpointAuthorization savePoliciesAuth()  { return commonAuth; }

    @Bean(AuthzBeans.NAMESPACES)
    public EndpointAuthorization namespacesAuth()    { return commonAuth; }
}
```

### Scenario B: Fine-Grained Authorization

If different endpoints need different rules, define specific logic per bean. The `AuthorizationContext` is a sealed interface with type-safe variants for each endpoint:

```java
@Configuration
public class AuthzEndpointConfig {

    @Bean(AuthzBeans.FIELDS)
    public EndpointAuthorization fieldsAuth() {
        return context -> {
            // Read-only — just requires authentication
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
}
```

The following `AuthorizationContext` subtypes are available for pattern matching:

| Bean | Context Type | Fields |
|---|---|---|
| `FIELDS` | `FieldsAuthContext` | `permissionCode` |
| `POLICIES` | `PoliciesAuthContext` | `subjectType`, `subjectId`, `namespace` |
| `SAVE_POLICIES` | `SavePoliciesAuthContext` | `request` (the full `SavePoliciesRequest`) |
| `NAMESPACES` | `NamespacesAuthContext` | *(none)* |
| `BUNDLE` | `BundleAuthContext` | `namespace` |
| `SUBJECTS` | `SubjectsAuthContext` | `type` |

### Scenario C: Mixed Approach

You can use library controllers for simple CRUD and write custom controllers for operations that need domain-specific logic. For example, activate the standard read endpoints but build a custom write controller:

```java
@Configuration
public class AuthzEndpointConfig {

    // Activate standard read endpoints (library controllers)
    @Bean(AuthzBeans.FIELDS)
    public EndpointAuthorization fieldsAuth() {
        return ctx -> requirePermission("READ_POLICIES");
    }

    @Bean(AuthzBeans.POLICIES)
    public EndpointAuthorization policiesAuth() {
        return ctx -> requirePermission("READ_POLICIES");
    }

    // SAVE_POLICIES is NOT activated — we build our own controller instead
}

// Custom write controller with domain-specific validation
@RestController
public class CustomSavePoliciesController {

    private final PolicyManagementService policyService;

    @PutMapping("/api/my-service/authz/policies")
    public void savePolicies(@RequestBody SavePoliciesRequest request) {
        // Custom validation, auditing, side-effects...
        policyService.savePolicies(request);
    }
}
```

### Overriding Auto-Configured Endpoints

To override the default Bundle authorization (e.g., to add custom security beyond API-key):

```java
@Bean(AuthzBeans.BUNDLE)
public EndpointAuthorization bundleAuth() {
    return context -> {
        if (context instanceof BundleAuthContext ctx) {
            if (!allowedNamespaces.contains(ctx.namespace())) {
                throw new AccessDeniedException("Namespace not allowed");
            }
        }
    };
}
```

To disable the Bundle endpoint entirely, set this in `application.yml`:

```yaml
datamate:
  authz:
    bundle:
      enabled: false
```
