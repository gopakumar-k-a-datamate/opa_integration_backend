package org.datamate.collaboration.config;

import com.datamate.bedrock.framework.common.exception.config.ExceptionProperties;
import com.datamate.bedrock.framework.common.exception.service.MessageResolver;
import com.datamate.bedrock.framework.common.exception.spring.service.web.GlobalExceptionHandler;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.collaboration.exception.ApplicationValidationException;
import org.datamate.collaboration.exception.CollaborationBaseException;
import org.datamate.collaboration.exception.DocumentConversionException;
import org.datamate.collaboration.exception.DomainValidationException;
import org.datamate.collaboration.exception.ResourceNotFoundException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;

/**
 * Global Exception Handler for the Collaboration Microservice.
 */
@RestControllerAdvice
public class CollaborationExceptionHandler extends GlobalExceptionHandler {

    @EnableLogger
    private Logger logger;
    
    private final MessageResolver messageResolver;
    private final ResourceBundleMessageSource fallbackMessageSource;

    public CollaborationExceptionHandler(MessageResolver resolver, ExceptionProperties properties) {
        super(resolver, properties);
        this.messageResolver = resolver;
        this.fallbackMessageSource = new ResourceBundleMessageSource();
        this.fallbackMessageSource.setBasename("messages");
        this.fallbackMessageSource.setDefaultEncoding("UTF-8");
        this.fallbackMessageSource.setUseCodeAsDefaultMessage(true);
    }
    
    private String resolveMessage(CollaborationBaseException ex) {
        String customMsg = ex.getCustomMessage();
        if (customMsg != null && !customMsg.isBlank()) {
            return customMsg;
        }
        String resolved = messageResolver.resolveMessage(ex.getErrorCode(), LocaleContextHolder.getLocale(), ex.getMessageArgs());
        if (resolved == null || resolved.equals(ex.getErrorCode())) {
             resolved = fallbackMessageSource.getMessage(ex.getErrorCode(), ex.getMessageArgs(), ex.getErrorCode(), LocaleContextHolder.getLocale());
        }
        return resolved != null ? resolved : ex.getErrorCode();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        String msg = resolveMessage(ex);
        if (logger != null) {
            logger.warn("Resource not found [errorCode: {}]: {}", ex.getErrorCode(), msg);
        }

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, msg);
        enrich(pd, ex);
        return pd;
    }

    @ExceptionHandler(DomainValidationException.class)
    public ProblemDetail handleDomainValidation(DomainValidationException ex) {
        String msg = resolveMessage(ex);
        if (logger != null) {
            logger.warn("Domain validation failed [errorCode: {}]: {}", ex.getErrorCode(), msg);
        }

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg);
        enrich(pd, ex);
        return pd;
    }

    @ExceptionHandler(ApplicationValidationException.class)
    public ProblemDetail handleApplicationValidation(ApplicationValidationException ex) {
        String msg = resolveMessage(ex);
        if (logger != null) {
            logger.warn("Application validation failed [errorCode: {}]: {}", ex.getErrorCode(), msg);
        }

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg);
        enrich(pd, ex);
        return pd;
    }

    @ExceptionHandler(DocumentConversionException.class)
    public ProblemDetail handleDocumentConversion(DocumentConversionException ex) {
        String msg = resolveMessage(ex);
        if (logger != null) {
            logger.error("Document conversion error [errorCode: {}]: {}", ex.getErrorCode(), msg);
        }

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, msg);
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
