package org.datamate.identity.domain.exception.role;

import org.datamate.identity.domain.exception.DomainException;

public class InvalidRoleDataException extends DomainException {

    public InvalidRoleDataException(String errorCode, String defaultMessage) {
        super(errorCode, defaultMessage);
    }
}
