package org.datamate.identity.domain.exception.role;

import org.datamate.identity.domain.exception.DomainException;

public class RoleNotFoundException extends DomainException {
    public RoleNotFoundException(String message) {
        super("role.notFound", message);
    }
}
