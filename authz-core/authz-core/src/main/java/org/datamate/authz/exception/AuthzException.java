package org.datamate.authz.exception;

import java.util.Map;

/**
 * Base exception for the Authz module.
 */
public abstract class AuthzException extends RuntimeException {

    private final AuthzErrorCode errorCode;
    private final transient Object[] messageArgs;
    private final String customMessage;
    private final Map<String, Object> metadata;

    protected AuthzException(AuthzErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
        this.messageArgs = null;
        this.metadata = null;
    }

    protected AuthzException(AuthzErrorCode errorCode, String customMessage) {
        super(customMessage != null ? customMessage : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.messageArgs = null;
        this.metadata = null;
    }

    protected AuthzException(AuthzErrorCode errorCode, String customMessage, Object... messageArgs) {
        super(customMessage != null ? customMessage : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.messageArgs = messageArgs != null ? messageArgs.clone() : null;
        this.metadata = null;
    }

    protected AuthzException(
            AuthzErrorCode errorCode,
            String customMessage,
            Object[] messageArgs,
            Map<String, Object> metadata) {

        super(customMessage != null ? customMessage : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.messageArgs = messageArgs != null ? messageArgs.clone() : null;
        this.metadata = metadata != null ? Map.copyOf(metadata) : null;
    }

    protected AuthzException(
            AuthzErrorCode errorCode,
            String customMessage,
            Object[] messageArgs,
            Map<String, Object> metadata,
            Throwable cause) {

        super(customMessage != null ? customMessage : errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.messageArgs = messageArgs != null ? messageArgs.clone() : null;
        this.metadata = metadata != null ? Map.copyOf(metadata) : null;
    }

    public AuthzErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public Object[] getMessageArgs() {
        return messageArgs != null ? messageArgs.clone() : null;
    }

    public Map<String, Object> getMetadata() {
        return metadata != null ? Map.copyOf(metadata) : null;
    }
}
