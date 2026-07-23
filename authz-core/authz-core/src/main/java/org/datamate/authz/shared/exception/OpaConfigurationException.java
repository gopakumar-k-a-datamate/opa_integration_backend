package org.datamate.authz.shared.exception;

public class OpaConfigurationException extends BaseException {

    public OpaConfigurationException(String message) {
        super("AUTHZ-1004", message);
    }
    
    public OpaConfigurationException(String message, Throwable cause) {
        super("AUTHZ-1004", message);
        this.initCause(cause);
    }
}
