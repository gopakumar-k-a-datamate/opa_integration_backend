package org.datamate.identity.user.domain.exception.user;

import org.datamate.identity.auth.domain.exception.DomainException;

public class InvalidRoleAssignmentException extends DomainException {
    public InvalidRoleAssignmentException(String roleName) {
        super("user.invalidRoleAssignment", "Cannot assign inactive role: " + roleName, roleName);
    }
}
