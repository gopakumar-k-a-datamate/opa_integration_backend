package org.datamate.pharmacy.adapter.in.rest.exception;

import com.datamate.bedrock.framework.common.exception.config.ExceptionProperties;
import com.datamate.bedrock.framework.common.exception.service.MessageResolver;
import com.datamate.bedrock.framework.common.exception.spring.service.web.GlobalExceptionHandler;
import org.datamate.authz.exception.AuthzException;
import com.datamate.bedrock.framework.common.exception.exceptions.BaseAppException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class PharmacyGlobalExceptionHandler extends GlobalExceptionHandler {

    public PharmacyGlobalExceptionHandler(MessageResolver resolver, ExceptionProperties properties) {
        super(resolver, properties);
    }

    @ExceptionHandler(AuthzException.class)
    public ProblemDetail handleAuthzException(AuthzException ex, HttpServletRequest request) {
        BaseAppException bedrockException = new BaseAppException(
                ex.getErrorCode().name(), 
                ex.getMessage(), 
                ex.getCause()
        );
        return handleBase(bedrockException, request);
    }
}
