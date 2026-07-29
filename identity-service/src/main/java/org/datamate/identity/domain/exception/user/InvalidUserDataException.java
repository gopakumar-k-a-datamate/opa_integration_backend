package org.datamate.identity.domain.exception.user;

import org.datamate.identity.domain.exception.DomainException;

public class InvalidUserDataException extends DomainException {
    public InvalidUserDataException(String message) {
        super("user.invalidData", message);
    }
}
