package org.datamate.pharmacy.adapter.in.rest.exception;

import com.datamate.bedrock.framework.common.exception.config.ExceptionProperties;
import com.datamate.bedrock.framework.common.exception.service.MessageResolver;
import com.datamate.bedrock.framework.common.exception.spring.service.web.GlobalExceptionHandler;
import org.datamate.authz.exception.AuthzException;
import org.datamate.authz.starter.exception.AuthzExceptionAdapter;
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
        // Adapt the pure AuthzException into the Bedrock flow
        return handleBase(new AuthzExceptionAdapter(ex), request);
    }
}
