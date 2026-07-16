package org.datamate.identity.domain.exception;

import org.datamate.identity.shared.exception.BaseException;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException(String message) {
        super("user.invalidCredentials", message);
    }
}
