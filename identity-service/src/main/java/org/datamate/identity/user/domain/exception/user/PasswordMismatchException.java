package org.datamate.identity.user.domain.exception.user;

import org.datamate.identity.user.domain.exception.domain.DomainException;

public class PasswordMismatchException extends DomainException {

    public PasswordMismatchException() {
        super("user.passwordMismatch", "The provided password does not match the current password.");
    }
}
