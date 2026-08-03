package org.datamate.authz.shared.exception;

import java.util.Map;

/**
 * Base exception for the Authz module.
 */
public abstract class BaseException extends RuntimeException {

    private final String errorCode;
    private final transient Object[] messageArgs;
    private final String customMessage;
    private final Map<String, Object> metadata;

    protected BaseException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.customMessage = null;
        this.messageArgs = null;
        this.metadata = null;
    }

    protected BaseException(String errorCode, String customMessage) {
        super(customMessage != null ? customMessage : errorCode);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.messageArgs = null;
        this.metadata = null;
    }

    protected BaseException(String errorCode, String customMessage, Object... messageArgs) {
        super(customMessage != null ? customMessage : errorCode);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.messageArgs = messageArgs != null ? messageArgs.clone() : null;
        this.metadata = null;
    }

    protected BaseException(
            String errorCode,
            String customMessage,
            Object[] messageArgs,
            Map<String, Object> metadata) {

        super(customMessage != null ? customMessage : errorCode);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.messageArgs = messageArgs != null ? messageArgs.clone() : null;
        this.metadata = metadata != null ? Map.copyOf(metadata) : null;
    }

    protected BaseException(
            String errorCode,
            String customMessage,
            Object[] messageArgs,
            Map<String, Object> metadata,
            Throwable cause) {

        super(customMessage != null ? customMessage : errorCode, cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
        this.messageArgs = messageArgs != null ? messageArgs.clone() : null;
        this.metadata = metadata != null ? Map.copyOf(metadata) : null;
    }

    public String getErrorCode() {
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
