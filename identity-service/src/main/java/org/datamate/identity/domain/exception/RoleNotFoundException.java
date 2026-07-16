package org.datamate.identity.domain.exception;

import org.datamate.identity.shared.exception.BaseException;

public class RoleNotFoundException extends BaseException {
    public RoleNotFoundException(String message) {
        super("role.notFound", message);
    }
}
