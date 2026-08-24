package org.datamate.identity.user.domain.exception;

import org.datamate.identity.user.domain.exception.DomainException;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException() {
        super("user.alreadyExists", "A user with this identity already exists.");
    }
}


