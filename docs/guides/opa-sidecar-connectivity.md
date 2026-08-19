# OPA Sidecar Connectivity (Evaluation URL)

## Context & Purpose
Our authorization architecture uses Open Policy Agent (OPA) deployed as a sidecar alongside each microservice. To evaluate permissions, the application must query this sidecar via HTTP REST calls. 

This document explains how the connection between the application and the OPA sidecar is configured using the `evaluation_url` property in `opa-config.yaml`, and how the `OpaRestTemplateAdapter` implements this communication.

---

## 1. Configuration (`opa-config.yaml`)

Each microservice (e.g., `finance-microservice`) has its own `opa-config.yaml` file. While this file is primarily used by the OPA agent itself to configure bundle polling, we also leverage it to store the application-side connectivity configuration.

```yaml
# opa-config.yaml
services:
  finance_api:
    url: http://host.docker.internal:8081

bundles:
  finance_bundle:
    service: finance_api
    resource: /internal/authz/bundle/finance
    polling:
      min_delay_seconds: 10
      max_delay_seconds: 20

default_authorization_decision: /app/authz/finance/allow

# Custom property read by the Java application
evaluation_url: http://localhost:8181/v1/data/app/authz/finance/allow
```

### The `evaluation_url` Property
The `evaluation_url` specifies the exact endpoint the Java application should HTTP `POST` to when evaluating a policy. 

**Dynamic Namespaces:** 
Because the application formats this URL at runtime (`String.format(evaluationUrl, namespace)`), the URL can optionally contain a `%s` placeholder for multi-module monoliths.
- *Hardcoded (Microservice):* `http://localhost:8181/v1/data/app/authz/finance/allow`
- *Dynamic (Modulith):* `http://localhost:8181/v1/data/app/authz/%s/allow`

---

## 2. Implementation (`OpaRestTemplateAdapter`)

The `OpaRestTemplateAdapter` is the Spring component responsible for parsing this configuration and executing the network call.

### 2.1. Startup Initialization
When the application context loads, the adapter reads the `opa-config.yaml` file:
1. It locates the file using the Spring property `${authz.opa.config.file:opa-config.yaml}`.
2. It uses Spring's `YamlPropertiesFactoryBean` to parse the YAML into Java `Properties`.
3. It extracts the `evaluation_url`. 
4. **Fail-Fast:** If the property is missing, or the file cannot be parsed, an `OpaConfigurationException` is thrown, halting application startup. This guarantees the application cannot boot in an insecure state where it doesn't know how to reach OPA.

### 2.2. Runtime Evaluation
When a protected endpoint is called, the AOP interceptor constructs an `OpaInputPayload` and calls `OpaRestTemplateAdapter.evaluate(...)`.

1. **URL Construction:** The adapter formats the `evaluation_url` injecting the `namespace`.
2. **HTTP POST:** It sends the JSON payload to the OPA sidecar using `RestTemplate`.
3. **Response Parsing:** It expects a successful 2xx response containing `{"result": true}` (or `{"result": {"allowed": true}}`). 
4. **Fail-Closed Security:** If OPA returns a non-200 response, returns unparseable JSON, or if the network connection fails entirely (`RestClientException`), the adapter logs a warning and returns `false`. This ensures a "fail-closed" security posture where access is strictly denied if the policy engine is unreachable.
