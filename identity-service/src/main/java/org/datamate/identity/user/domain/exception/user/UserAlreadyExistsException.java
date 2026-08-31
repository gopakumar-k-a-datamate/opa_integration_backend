package org.datamate.identity.user.domain.exception.user;

import org.datamate.identity.user.domain.exception.domain.DomainException;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException() {
        super("user.alreadyExists", "A user with this identity already exists.");
    }
}
