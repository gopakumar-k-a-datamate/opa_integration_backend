package org.datamate.identity.domain.exception;

import org.datamate.identity.shared.exception.BaseException;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("user.invalidCredentials", "Invalid username or password.");
    }
}
