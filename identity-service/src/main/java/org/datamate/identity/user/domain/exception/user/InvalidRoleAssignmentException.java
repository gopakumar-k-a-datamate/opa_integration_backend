package org.datamate.identity.user.domain.exception.user;

import org.datamate.identity.user.domain.exception.domain.DomainException;

public class InvalidRoleAssignmentException extends DomainException {
    public InvalidRoleAssignmentException(String roleName) {
        super("user.invalidRoleAssignment", "Cannot assign inactive role: " + roleName, roleName);
    }

    public InvalidRoleAssignmentException(String message, String roleName) {
        super("user.invalidRoleAssignment", message, roleName);
    }
}
