package org.datamate.identity.role.domain.exception;

import org.datamate.identity.user.domain.exception.DomainException;

public class RoleNotFoundException extends DomainException {

    public RoleNotFoundException() {
        super("role.notFound", "Role not found.");
    }
}


