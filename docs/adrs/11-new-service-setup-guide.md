# 11. New Service Setup Guide

## Context & Purpose
Our authorization framework is decentralized. When establishing a new microservice or modulith, the service is entirely responsible for hosting its own authorization database tables, bundle compilation cache, and OPA sidecar. 

This guide serves as a checklist and baseline reference for the foundational setup required when integrating the authorization framework into a new service.

---

## 1. Project Dependencies

To leverage the core authorization logic, AOP interceptors, and database adapters, you must include the bedrock authorization starter in your project's build file.

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

Because we follow a **Database-First** paradigm, your new service must physically own and manage its authorization schema.

1. **Base Tables:** Copy or include the initial Flyway migration script (e.g., `V1__create_authz_tables.sql`) to create the `authz_resource`, `authz_permission`, `authz_condition_field`, and `authz_policy` tables.
2. **Populate Data:** Create subsequent migration scripts (e.g., `V2__insert_domain_resources.sql`) to register your specific domain resources, permissions, and condition fields. (Refer to the `09-database-first-migration-guide.md` for specific SQL examples).

---

## 3. OPA Sidecar Configuration (`opa-config.yaml`)

You must create an `opa-config.yaml` file at the root of your project. This single file is used both by the OPA container to configure bundle polling, and by the Java application (`OpaRestTemplateAdapter`) to locate the sidecar.

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
# The endpoint the application will POST to for policy evaluation
evaluation_url: http://localhost:8181/v1/data/app/authz/<your_namespace>/allow
```
*(Replace `<your_namespace>` and the `8080` port to match your specific service).*

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

## 5. Application Code (Annotations)

Finally, link your application's domain commands to the database schema. Annotate the commands that pass through your application layer to trigger the AOP policy interceptor.

```java
@PolicyResource(namespace = "<your_namespace>", name = "my_resource", action = "read")
public record MyDomainCommand(
    
    @PolicyField(displayName = "Context Field", type = FieldType.STRING)
    String someField

) {}
```
Remember: These annotations act **purely as runtime markers** to extract data for the OPA evaluation payload. They do not auto-register anything in the database!
