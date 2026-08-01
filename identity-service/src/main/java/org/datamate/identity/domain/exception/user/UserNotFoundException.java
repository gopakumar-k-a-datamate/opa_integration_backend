package org.datamate.identity.domain.exception.user;

import org.datamate.identity.domain.exception.DomainException;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(String message) {
        super("user.notFound", message);
    }
}
