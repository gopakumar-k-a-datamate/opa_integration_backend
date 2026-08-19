package org.datamate.identity.domain.exception.role;

import org.datamate.identity.domain.exception.DomainException;

public class RoleAlreadyExistsException extends DomainException {

    public RoleAlreadyExistsException() {
        super("role.alreadyExists", "A role with this name already exists.");
    }
}
