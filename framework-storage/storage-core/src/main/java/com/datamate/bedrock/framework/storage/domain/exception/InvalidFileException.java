package com.datamate.bedrock.framework.storage.domain.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════
 * INVALID FILE EXCEPTION
 * ═══════════════════════════════════════════════════════════════
 * 
 * Thrown when a file fails validation
 * 
 * VALIDATION FAILURES:
 * - File extension not allowed (e.g., .exe when only .pdf allowed)
 * - File too large (e.g., 20 MB when max is 10 MB)
 * - File is empty (0 bytes)
 * - Invalid content type
 * 
 * ═══════════════════════════════════════════════════════════════
 */
public class InvalidFileException extends StorageException {

    private final List<String> validationErrors;

    /**
     * Create exception with single error message
     * 
     * Example:
     *   throw new InvalidFileException("File is too large");
     */
    public InvalidFileException(String message) {
        super(message);
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(message);
    }

    /**
     * Create exception with multiple validation errors
     * 
     * Example:
     *   List<String> errors = Arrays.asList(
     *       "Invalid file extension",
     *       "File too large"
     *   );
     *   throw new InvalidFileException(errors);
     */
    public InvalidFileException(List<String> validationErrors) {
        super("File validation failed: " + String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
    }

    /**
     * Get all validation errors
     * 
     * @return List of error messages
     */
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
