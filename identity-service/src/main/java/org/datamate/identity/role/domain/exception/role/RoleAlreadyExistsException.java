package org.datamate.identity.role.domain.exception.role;

import org.datamate.identity.auth.domain.exception.DomainException;

public class RoleAlreadyExistsException extends DomainException {

    public RoleAlreadyExistsException() {
        super("role.alreadyExists", "A role with this name already exists.");
    }
}
