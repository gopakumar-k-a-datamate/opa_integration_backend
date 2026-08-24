package org.datamate.identity.user.domain.exception;

import org.datamate.identity.user.domain.exception.DomainException;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException() {
        super("user.notFound", "User not found.");
    }
}


