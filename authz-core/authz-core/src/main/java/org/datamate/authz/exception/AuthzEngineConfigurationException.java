package org.datamate.authz.exception;

public class AuthzEngineConfigurationException extends AuthzException {

    public AuthzEngineConfigurationException(String message) {
        super(AuthzErrorCode.ENGINE_CONFIG_ERROR, message);
    }
    
    public AuthzEngineConfigurationException(String message, Throwable cause) {
        super(AuthzErrorCode.ENGINE_CONFIG_ERROR, message, null, null, cause);
    }
}
