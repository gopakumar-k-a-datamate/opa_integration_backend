package org.datamate.identity.domain.exception.authorization;

public class NamespaceNotFoundException extends RuntimeException {
    public NamespaceNotFoundException(String message) {
        super(message);
    }
}
