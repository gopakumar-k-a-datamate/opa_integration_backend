package org.datamate.identity.domain.exception;

import org.datamate.identity.shared.exception.BaseException;

public class RoleAlreadyExistsException extends DomainException {
    public RoleAlreadyExistsException(String message) {
        super("role.alreadyExists", message);
    }
}
