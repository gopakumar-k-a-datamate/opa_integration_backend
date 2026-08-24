package org.datamate.identity.user.domain.exception;

import org.datamate.identity.user.domain.exception.DomainException;

public class UserAccessDeniedException extends DomainException {

    public UserAccessDeniedException() {
        super("user.accessDenied", "You do not have permission to perform this action.");
    }
}


