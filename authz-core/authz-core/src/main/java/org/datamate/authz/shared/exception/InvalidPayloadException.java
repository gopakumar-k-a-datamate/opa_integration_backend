package org.datamate.authz.shared.exception;

/**
 * Thrown when the incoming policy request payload is invalid or malformed.
 */
public class InvalidPayloadException extends BaseException {

    public InvalidPayloadException(String message) {
        super("AUTHZ_INVALID_PAYLOAD", message);
    }
}
