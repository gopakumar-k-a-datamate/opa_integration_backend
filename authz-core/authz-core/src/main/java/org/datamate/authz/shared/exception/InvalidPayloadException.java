package org.datamate.authz.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the incoming policy request payload is invalid or malformed.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPayloadException extends BaseException {

    public InvalidPayloadException(String message) {
        super("AUTHZ_INVALID_PAYLOAD", message);
    }
}
