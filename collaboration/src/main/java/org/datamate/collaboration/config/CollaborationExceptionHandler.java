package org.datamate.collaboration.config;

import com.datamate.bedrock.framework.common.exception.config.ExceptionProperties;
import com.datamate.bedrock.framework.common.exception.service.MessageResolver;
import com.datamate.bedrock.framework.common.exception.spring.service.web.GlobalExceptionHandler;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.collaboration.exception.CollaborationBaseException;
import org.datamate.collaboration.exception.DomainValidationException;
import org.datamate.collaboration.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;

/**
 * Global Exception Handler for the Collaboration Microservice.
 * <p>
 * Extends Bedrock's {@link GlobalExceptionHandler} and adds local
 * {@code @ExceptionHandler} methods for intent-based collaboration exceptions
 * that require specific HTTP status codes not covered by Bedrock's
 * generic severity-based mapping.
 * <p>
 * Mapping:
 * <ul>
 *   <li>{@link ResourceNotFoundException} → HTTP 404 Not Found</li>
 *   <li>{@link DomainValidationException} → HTTP 400 Bad Request</li>
 *   <li>All other {@code BaseAppException} subclasses → Bedrock's severity-based mapping (inherited)</li>
 * </ul>
 */
@RestControllerAdvice
public class CollaborationExceptionHandler extends GlobalExceptionHandler {

    @EnableLogger
    private Logger logger;

    public CollaborationExceptionHandler(MessageResolver resolver, ExceptionProperties properties) {
        super(resolver, properties);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Resource not found [errorCode: {}]: {}", ex.getErrorCode(), ex.getMessage());

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        enrich(pd, ex);
        return pd;
    }

    @ExceptionHandler(DomainValidationException.class)
    public ProblemDetail handleDomainValidation(DomainValidationException ex) {
        logger.warn("Domain validation failed [errorCode: {}]: {}", ex.getErrorCode(), ex.getMessage());

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        enrich(pd, ex);
        return pd;
    }

    /**
     * Enriches a ProblemDetail with standard Collaboration error metadata.
     */
    private void enrich(ProblemDetail pd, CollaborationBaseException ex) {
        pd.setType(URI.create("about:blank"));
        pd.setTitle(ex.getErrorCode());
        pd.setProperty("errorCode", ex.getErrorCode());
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
    }
}
