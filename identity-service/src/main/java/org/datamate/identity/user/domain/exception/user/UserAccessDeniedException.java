package org.datamate.identity.user.domain.exception.user;

import org.datamate.identity.user.domain.exception.domain.DomainException;

public class UserAccessDeniedException extends DomainException {

    public UserAccessDeniedException() {
        super("user.accessDenied", "You do not have permission to perform this action.");
    }
}
