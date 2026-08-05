package org.datamate.identity.domain.exception;

import org.datamate.identity.shared.exception.BaseException;

public class UserAlreadyExistsException extends BaseException {
    public UserAlreadyExistsException(String message) {
        super("user.alreadyExists", message);
    }
}
