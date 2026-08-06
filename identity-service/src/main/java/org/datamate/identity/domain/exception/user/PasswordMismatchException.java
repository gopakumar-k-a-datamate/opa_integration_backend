package org.datamate.identity.domain.exception.user;

import org.datamate.identity.domain.exception.DomainException;

public class PasswordMismatchException extends DomainException {
    public PasswordMismatchException(String message) {
        super("user.passwordMismatch", message);
    }
}
