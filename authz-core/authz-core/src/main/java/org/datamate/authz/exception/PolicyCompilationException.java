package org.datamate.authz.exception;

public class PolicyCompilationException extends AuthzException {
    public PolicyCompilationException(String message) {
        super(AuthzErrorCode.COMPILATION_ERROR, message);
    }
    
    public PolicyCompilationException(String message, Throwable cause) {
        super(AuthzErrorCode.COMPILATION_ERROR, message, null, null, cause);
    }
}
