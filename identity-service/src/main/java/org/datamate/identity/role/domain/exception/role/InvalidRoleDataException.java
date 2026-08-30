package org.datamate.identity.role.domain.exception.role;

import org.datamate.identity.auth.domain.exception.DomainException;

public class InvalidRoleDataException extends DomainException {

    public InvalidRoleDataException(String errorCode, String defaultMessage) {
        super(errorCode, defaultMessage);
    }
}
