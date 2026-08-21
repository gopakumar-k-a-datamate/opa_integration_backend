package org.datamate.pharmacy.adapter.in.rest.exception;

import com.datamate.bedrock.framework.common.exception.config.ExceptionProperties;
import com.datamate.bedrock.framework.common.exception.service.MessageResolver;
import com.datamate.bedrock.framework.common.exception.spring.service.web.GlobalExceptionHandler;
import com.datamate.bedrock.framework.common.exception.exceptions.BaseAppException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;

@RestControllerAdvice
public class PharmacyGlobalExceptionHandler extends GlobalExceptionHandler {

    public PharmacyGlobalExceptionHandler(MessageResolver resolver, ExceptionProperties properties) {
        super(resolver, properties);
    }

    @EnableLogger
    private Logger logger;

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpServletRequest request) {
        logger.warn("Resource not found: {}", ex.getResourcePath());
        org.springframework.http.ProblemDetail pd = org.springframework.http.ProblemDetail.forStatusAndDetail(
                org.springframework.http.HttpStatus.NOT_FOUND, 
                "The requested endpoint does not exist."
        );
        pd.setTitle("NOT_FOUND");
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAny(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception occurred", ex);
        org.springframework.http.ProblemDetail pd = org.springframework.http.ProblemDetail.forStatusAndDetail(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred"
        );
        pd.setTitle("INTERNAL_SERVER_ERROR");
        return pd;
    }
}
