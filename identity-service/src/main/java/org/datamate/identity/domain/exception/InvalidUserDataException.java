package org.datamate.identity.domain.exception;

import org.datamate.identity.shared.exception.BaseException;

public class InvalidUserDataException extends BaseException {
    public InvalidUserDataException(String message) {
        super("user.invalidData", message);
    }
}
