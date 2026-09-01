package org.datamate.identity.user.domain.exception.user;

import org.datamate.identity.user.domain.exception.domain.DomainException;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException() {
        super("user.notFound", "User not found.");
    }
}
