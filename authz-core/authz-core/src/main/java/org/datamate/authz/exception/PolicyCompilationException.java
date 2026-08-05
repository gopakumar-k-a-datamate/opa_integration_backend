package org.datamate.authz.exception;

public class PolicyCompilationException extends RuntimeException {
    public PolicyCompilationException(String message) {
        super(message);
    }
    
    public PolicyCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}
