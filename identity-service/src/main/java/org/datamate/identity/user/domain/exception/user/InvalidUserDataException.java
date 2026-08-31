package org.datamate.identity.user.domain.exception.user;

import org.datamate.identity.user.domain.exception.domain.DomainException;

public class InvalidUserDataException extends DomainException {

    public InvalidUserDataException(String errorCode, String defaultMessage) {
        super(errorCode, defaultMessage);
    }
}
