package org.datamate.identity.role.domain.exception;

import org.datamate.identity.user.domain.exception.DomainException;

public class InvalidRoleDataException extends DomainException {

    public InvalidRoleDataException(String errorCode, String defaultMessage) {
        super(errorCode, defaultMessage);
    }
}


