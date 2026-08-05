package org.datamate.identity.domain.exception.user;

import org.datamate.identity.domain.exception.DomainException;

public class UserAccessDeniedException extends DomainException {
    public UserAccessDeniedException(String message) {
        super("user.accessDenied", message);
    }
}
