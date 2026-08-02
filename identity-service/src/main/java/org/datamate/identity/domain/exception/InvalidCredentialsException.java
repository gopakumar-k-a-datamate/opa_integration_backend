package org.datamate.identity.domain.exception;

import org.datamate.identity.shared.exception.BaseException;

public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException(String message) {
        super("user.invalidCredentials", message);
    }
}
