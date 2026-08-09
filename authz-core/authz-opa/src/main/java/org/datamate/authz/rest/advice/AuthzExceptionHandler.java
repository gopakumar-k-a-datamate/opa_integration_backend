package org.datamate.authz.rest.advice;

import org.datamate.authz.exception.InvalidPolicySyntaxException;
import org.datamate.authz.model.policy.valueobject.RegoValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class AuthzExceptionHandler {

    @ExceptionHandler(InvalidPolicySyntaxException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPolicySyntax(InvalidPolicySyntaxException ex) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                ex.getPermissionCode(),
                ex.getErrors(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    public record ErrorResponse(
            int status,
            String message,
            String permissionCode,
            List<RegoValidationError> errors,
            LocalDateTime timestamp
    ) {}
}
