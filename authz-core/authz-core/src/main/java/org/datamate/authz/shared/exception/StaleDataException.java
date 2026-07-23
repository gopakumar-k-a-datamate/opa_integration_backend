package org.datamate.authz.shared.exception;

public class StaleDataException extends BaseException {
    public StaleDataException(String defaultMessage) {
        // Assuming AUTHZ-409 is a recognizable code
        super("AUTHZ-409", defaultMessage);
    }
}
