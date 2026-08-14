package org.datamate.authz.starter.exception;

import com.datamate.bedrock.framework.common.exception.exceptions.BaseAppException;
import org.datamate.authz.exception.AuthzException;
import org.datamate.authz.exception.AuthzInvalidSyntaxException;

/**
 * Adapter to seamlessly bridge pure AuthzExceptions into the Datamate Bedrock
 * exception management ecosystem.
 */
public class AuthzExceptionAdapter extends BaseAppException {
    
    private final AuthzException originalException;

    public AuthzExceptionAdapter(AuthzException ex) {
        super(ex.getErrorCode().name(), ex.getMessage(), ex.getCause());
        this.originalException = ex;
    }

    public AuthzException getOriginalException() {
        return originalException;
    }
}
