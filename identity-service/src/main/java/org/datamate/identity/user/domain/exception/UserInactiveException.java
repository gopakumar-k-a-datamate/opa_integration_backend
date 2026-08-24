package org.datamate.identity.user.domain.exception;

import org.datamate.identity.user.domain.exception.DomainException;

public class UserInactiveException extends DomainException {

    public UserInactiveException() {
        super("user.inactive", "User account is inactive. Please contact administration.");
    }
}


