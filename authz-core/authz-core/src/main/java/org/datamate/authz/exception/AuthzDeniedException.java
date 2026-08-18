package org.datamate.authz.exception;

public class AuthzDeniedException extends AuthzException {

    public AuthzDeniedException(String message) {
        super(AuthzErrorCode.DENIED, message);
    }

    public AuthzDeniedException(String message, Throwable cause) {
        super(AuthzErrorCode.DENIED, message, null, null, cause);
    }
}
