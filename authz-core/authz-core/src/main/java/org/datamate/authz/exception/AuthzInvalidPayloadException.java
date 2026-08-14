package org.datamate.authz.exception;

/**
 * Thrown when the incoming policy request payload is invalid or malformed.
 */
public class AuthzInvalidPayloadException extends AuthzException {

    public AuthzInvalidPayloadException(String message) {
        super(AuthzErrorCode.INVALID_PAYLOAD, message);
    }

    public AuthzInvalidPayloadException(String message, Throwable cause) {
        super(AuthzErrorCode.INVALID_PAYLOAD, message, null, null, cause);
    }
}
