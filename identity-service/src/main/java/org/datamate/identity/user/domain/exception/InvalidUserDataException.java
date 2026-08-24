package org.datamate.identity.user.domain.exception;

import org.datamate.identity.user.domain.exception.DomainException;

public class InvalidUserDataException extends DomainException {

    public InvalidUserDataException(String errorCode, String defaultMessage) {
        super(errorCode, defaultMessage);
    }
}


