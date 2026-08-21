package org.datamate.authz.rest.exception;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import jakarta.servlet.http.HttpServletRequest;
import org.datamate.authz.exception.AuthzException;
import org.datamate.authz.exception.AuthzInvalidSyntaxException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "org.datamate.authz.rest.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthzLibraryExceptionHandler {

    @EnableLogger
    private Logger logger;

    @ExceptionHandler(AuthzInvalidSyntaxException.class)
    public ProblemDetail handleAuthzInvalidSyntaxException(AuthzInvalidSyntaxException ex, HttpServletRequest request) {
        logger.error("Authz syntax exception occurred", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle(ex.getErrorCode().name());
        pd.setProperty("permissionCode", ex.getPermissionCode());
        pd.setProperty("errors", ex.getErrors());
        return pd;
    }

    @ExceptionHandler(AuthzException.class)
    public ProblemDetail handleAuthzException(AuthzException ex, HttpServletRequest request) {
        logger.error("Authz exception occurred", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle(ex.getErrorCode().name());
        return pd;
    }
}
