package org.datamate.authz.model.policy.valueobject;

/**
 * A single syntax error in a Rego snippet, with exact location for
 * the UI code editor to highlight.
 */
public record RegoValidationError(
        int line,
        int column,
        String message
) {}
