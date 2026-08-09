package org.datamate.authz.model.policy.valueobject;

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
