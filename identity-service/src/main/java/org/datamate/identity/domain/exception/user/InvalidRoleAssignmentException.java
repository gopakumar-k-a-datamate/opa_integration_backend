package org.datamate.identity.domain.exception.user;

import org.datamate.identity.domain.exception.DomainException;

public class InvalidRoleAssignmentException extends DomainException {
    public InvalidRoleAssignmentException(String message) {
        super("user.invalidRoleAssignment", message);
    }
}
