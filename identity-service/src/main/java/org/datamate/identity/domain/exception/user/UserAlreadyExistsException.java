package org.datamate.identity.domain.exception.user;

import org.datamate.identity.domain.exception.DomainException;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(String message) {
        super("user.alreadyExists", message);
    }
}
