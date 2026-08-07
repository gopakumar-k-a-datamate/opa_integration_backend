package org.datamate.identity.domain.exception.role;

import org.datamate.identity.domain.exception.DomainException;

public class RoleAlreadyExistsException extends DomainException {
    public RoleAlreadyExistsException(String message) {
        super("role.alreadyExists", message);
    }
}
