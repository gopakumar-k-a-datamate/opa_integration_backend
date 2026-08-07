package org.datamate.identity.domain.exception.user;

import org.datamate.identity.domain.exception.DomainException;

public class UserAccessDeniedException extends DomainException {

    public UserAccessDeniedException() {
        super("user.accessDenied", "You do not have permission to perform this action.");
    }
}
