package org.datamate.identity.domain.exception.user;

import org.datamate.identity.domain.exception.DomainException;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException() {
        super("user.notFound", "User not found.");
    }
}
