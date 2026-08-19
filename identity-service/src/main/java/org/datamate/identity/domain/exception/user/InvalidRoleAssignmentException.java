package org.datamate.identity.domain.exception.user;

import org.datamate.identity.domain.exception.DomainException;

public class InvalidRoleAssignmentException extends DomainException {
    public InvalidRoleAssignmentException(String roleName) {
        super("user.invalidRoleAssignment", "Cannot assign inactive role: " + roleName, roleName);
    }
}
