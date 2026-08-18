package org.datamate.authz.exception;

/**
 * Standardized error codes for the Authz framework.
 */
public enum AuthzErrorCode {
    DENIED("Access denied by policy."),
    INVALID_PAYLOAD("The requested payload is invalid or malformed."),
    INVALID_SYNTAX("The policy syntax is invalid."),
    STALE_DATA("The data is stale or has been modified by another process."),
    COMPILATION_ERROR("An error occurred during policy compilation."),
    ENGINE_CONFIG_ERROR("The authorization engine is misconfigured.");

    private final String defaultMessage;

    AuthzErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
