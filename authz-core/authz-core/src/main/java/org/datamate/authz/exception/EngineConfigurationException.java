package org.datamate.authz.exception;

public class EngineConfigurationException extends BaseException {

    public EngineConfigurationException(String message) {
        super("AUTHZ-1004", message);
    }
    
    public EngineConfigurationException(String message, Throwable cause) {
        super("AUTHZ-1004", message);
        this.initCause(cause);
    }
}
