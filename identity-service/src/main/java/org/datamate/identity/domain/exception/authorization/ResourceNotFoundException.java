package org.datamate.identity.domain.exception.authorization;

import org.datamate.identity.shared.exception.BaseException;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super("resource.notFound", message);
    }
}
