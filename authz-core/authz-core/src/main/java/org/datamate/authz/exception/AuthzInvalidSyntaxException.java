package org.datamate.authz.exception;

import org.datamate.authz.model.policy.valueobject.RegoValidationError;

import java.util.List;

/**
 * Thrown when a custom Rego snippet fails syntax validation.
 * Contains structured error information for the UI code editor.
 */
public class AuthzInvalidSyntaxException extends AuthzException {

    private final String permissionCode;
    private final transient List<RegoValidationError> errors;

    public AuthzInvalidSyntaxException(String permissionCode, List<RegoValidationError> errors) {
        super(AuthzErrorCode.INVALID_SYNTAX, "Custom Rego snippet for " + permissionCode + " has " + errors.size() + " syntax error(s)");
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
