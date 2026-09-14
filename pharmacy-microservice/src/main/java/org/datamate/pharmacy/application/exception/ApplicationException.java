package org.datamate.pharmacy.application.exception;

import org.datamate.pharmacy.shared.exception.BaseException;

public class ApplicationException extends BaseException {

    public ApplicationException(String errorCode) {
        super(errorCode);
    }

    public ApplicationException(String errorCode, String defaultMessage) {
        super(errorCode, defaultMessage);
    }

    public ApplicationException(String errorCode, String defaultMessage, Object... args) {
        super(errorCode, defaultMessage, args);
    }
}
