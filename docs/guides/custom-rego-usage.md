# Custom Rego Usage Guide

## Overview

The Authorization Engine provides a visual Condition Builder for most standard access control policies. However, for complex requirements (e.g., field-to-field comparisons, nested array evaluations, or time-based rules), Administrators can write **Custom Rego Snippets**. 

When Custom Rego is enabled for a policy, the system seamlessly stitches your custom logic into the globally compiled OPA bundle.

## How It Works

You do not need to write a full OPA Rego file. The system automatically manages the environment for you. Your custom snippet is injected into a pre-configured template that automatically handles the `package` definition, necessary `import` statements, and the core evaluation logic.

The final evaluation strictly follows this logic:
> **Access is granted IF (`allow_rule` is true)**

## Writing Your Rules

To define your logic, you must write conditions for `allow_rule`. You can write as many rules as you need; multiple rules of the same name are evaluated as a logical **OR**.

### The `allow_rule`
This rule defines when access should be granted.
```rego
allow_rule if {
    input.user.attributes.department == "Cardiology"
    input.resource.patientAge >= 18
}
```

## Available Input Context

Inside your Custom Rego, you have access to the standard `input` payload provided during an authorization request:

- **`input.user`**: The user requesting access.
  - `input.user.id`: (String) The user's ID.
  - `input.user.roles`: (Array of Strings) The user's assigned roles.
  - `input.user.attributes`: (Object) Additional user attributes (e.g., department, clearance level).
- **`input.resource`**: The resource being accessed. This contains any properties exposed by the system via `@PolicyField`.
- **`input.permission`**: (String) The specific permission being checked (e.g., `pharmacy:prescription:read`).

## Strict Restrictions (What NOT to do)

To maintain system stability and security, the system enforces several strict rules on Custom Rego snippets. If you violate these, the system will reject your snippet with a Validation Error.

1. **No `package` declarations**: The compiler automatically assigns the correct namespace to prevent collisions.
2. **No `import` statements**: The compiler automatically imports `rego.v1` and any other required dependencies.
3. **No `default` assignments**: The system automatically sets the defaults (e.g., `default allow := false`) to ensure deterministic behavior.
4. **Do NOT define the `allow` rule**: You are not permitted to define `allow if { ... }`, `allow { ... }`, or `allow = true`. You must strictly use `allow_rule if`. Overriding the core `allow` rule breaks the centralized security architecture.

## Validation

When you attempt to save a Custom Rego snippet, the system runs a strict syntax check against an OPA sidecar. 
If your snippet contains invalid Rego syntax, references restricted keywords, or violates any of the rules above, the save operation will be blocked and you will receive a clear error message indicating exactly what needs to be fixed.
