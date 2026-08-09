package org.datamate.authz.exception;

import org.datamate.authz.model.policy.valueobject.RegoValidationError;

import java.util.List;

/**
 * Thrown when a custom Rego snippet fails syntax validation.
 * Contains structured error information for the UI code editor.
 */
public class InvalidPolicySyntaxException extends RuntimeException {

    private final List<RegoValidationError> errors;
    private final String permissionCode;

    public InvalidPolicySyntaxException(String permissionCode, List<RegoValidationError> errors) {
        super("Custom Rego snippet for " + permissionCode + " has " + errors.size() + " syntax error(s)");
        this.permissionCode = permissionCode;
        this.errors = errors;
    }

    public List<RegoValidationError> getErrors() {
        return errors;
    }

    public String getPermissionCode() {
        return permissionCode;
    }
}
