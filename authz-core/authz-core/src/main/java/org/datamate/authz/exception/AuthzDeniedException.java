package org.datamate.authz.exception;

public class AuthzDeniedException extends RuntimeException {

    public AuthzDeniedException(String message) {
        super(message);
    }

    public AuthzDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
