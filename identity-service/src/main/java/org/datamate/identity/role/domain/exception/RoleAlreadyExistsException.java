package org.datamate.identity.role.domain.exception;

import org.datamate.identity.user.domain.exception.DomainException;

public class RoleAlreadyExistsException extends DomainException {

    public RoleAlreadyExistsException() {
        super("role.alreadyExists", "A role with this name already exists.");
    }
}


