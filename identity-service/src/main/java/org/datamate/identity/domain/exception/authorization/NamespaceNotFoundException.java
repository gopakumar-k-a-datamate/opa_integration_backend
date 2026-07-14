package org.datamate.identity.domain.exception.authorization;

import org.datamate.identity.shared.exception.BaseException;

public class NamespaceNotFoundException extends BaseException {
    public NamespaceNotFoundException(String message) {
        super("namespace.notFound", message);
    }
}
