# Rego Compilation and Generation

One of the key features of `authz-core` is the ability to generate Open Policy Agent (OPA) policies dynamically. Instead of manually writing Rego files, administrators can construct policy conditions in a UI, which the framework translates into a Java Abstract Syntax Tree (AST) and compiles into Rego code.

## The AST Model

The framework models logical conditions using the `AstNode` class structure in the `org.datamate.authz.compiler.ast` package. 

### Core Components:
- **`ConditionNode`**: Represents a single evaluation rule (e.g., `user.role == 'ADMIN'`). It contains a field, an operator (e.g., `EQUALS`, `IN`), and a value.
- **`GroupNode`**: Represents a logical grouping of nodes (e.g., `Node A AND Node B`). It contains a `LogicalOperator` (AND / OR) and a list of child `AstNode`s.

## The Compiler (`DefaultPolicyCompiler`)

The compiler reads raw policies (often persisted in a database) and translates them into an executable Rego bundle.

1. **Namespace Grouping:** Policies are grouped by their namespace (e.g., `pharmacy`, `finance`). Each namespace becomes a separate Rego package (e.g., `package app.authz.pharmacy`).
2. **Rule Generation:** For each policy within a namespace, the `RegoGenerator` evaluates the AST and produces a Rego rule block.

## The Generator (`RegoGenerator`)

The `RegoGenerator` takes a `GroupNode` and recursively outputs raw Rego strings. 

### Generation Logic:
- **Base Structure:** Every compiled namespace receives default declarations to fail-closed:
  ```rego
  default allow := false
  default allow_rule := false
  default deny_rule := false
  ```
- **Allow and Deny Rules:** The generator creates `allow_rule if { ... }` blocks for policies mapped to the `ALLOW` effect, and `deny_rule if { ... }` blocks for policies mapped to the `DENY` effect.
- **Permissions:** Every rule block automatically enforces the permission code:
  ```rego
  input.permission == "namespace:resource:action"
  ```

### Example

A policy stating: *"Allow if user has role ADMIN OR (role DOCTOR AND age > 25)"* for the `pharmacy:prescription:create` permission generates the following structure:

```rego
# Rule 1: Admin
allow_rule if {
    "ADMIN" in input.user.roles
    input.permission == "pharmacy:prescription:create"
}

# Rule 2: Doctor over 25
allow_rule if {
    "DOCTOR" in input.user.roles
    input.resource.age > 25
    input.permission == "pharmacy:prescription:create"
}

allow if {
    allow_rule
    not deny_rule
}
```

### Supported Operators
The AST builder supports mapping Java/UI operators to Rego operators:
- `EQUALS` -> `==`
- `NOT_EQUALS` -> `!=`
- `GREATER_THAN` -> `>`
- `LESS_THAN` -> `<`
- `IN` -> `in` (for lists/arrays)
- `NOT_IN` -> `not in`

## Best Practices
- **Attribute Referencing:** Fields targeting the user must be prefixed with `user.` (e.g., `user.id`), which compiles to `input.user.id`. Fields targeting the resource payload must be prefixed with `resource.` (e.g., `resource.amount`), compiling to `input.resource.amount`.
