package org.datamate.authz.exception;

public class AuthzStaleDataException extends AuthzException {
    public AuthzStaleDataException(String defaultMessage) {
        super(AuthzErrorCode.STALE_DATA, defaultMessage);
    }
}
