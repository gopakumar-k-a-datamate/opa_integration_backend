package org.datamate.identity.role.domain.exception.role;

import org.datamate.identity.domain.exception.DomainException;

public class RoleNotFoundException extends DomainException {

    public RoleNotFoundException() {
        super("role.notFound", "Role not found.");
    }
}
