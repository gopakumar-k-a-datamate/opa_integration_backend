package org.datamate.authz.shared.exception;

import com.datamate.bedrock.framework.common.exception.exceptions.BaseAppException;

/**
 * Base exception for the Authz module.
 */
public abstract class BaseException extends BaseAppException {

    protected BaseException(String errorCode) {
        super(errorCode);
    }

    protected BaseException(String errorCode, String defaultMessage) {
        super(errorCode, defaultMessage);
    }

    protected BaseException(String errorCode, String defaultMessage, Object... args) {
        super(errorCode, defaultMessage, args);
    }
}
