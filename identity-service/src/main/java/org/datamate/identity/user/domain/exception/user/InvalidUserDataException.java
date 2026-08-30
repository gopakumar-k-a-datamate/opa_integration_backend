package org.datamate.identity.user.domain.exception.user;

import org.datamate.identity.auth.domain.exception.DomainException;

public class InvalidUserDataException extends DomainException {

    public InvalidUserDataException(String errorCode, String defaultMessage) {
        super(errorCode, defaultMessage);
    }
}
