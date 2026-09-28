package com.datamate.bedrock.framework.storage.domain.exception;

/**
 * ═══════════════════════════════════════════════════════════════
 * STORAGE EXCEPTION - Base exception for all storage errors
 * ═══════════════════════════════════════════════════════════════
 * 
 * WHY CUSTOM EXCEPTIONS?
 * - Hide MinIO-specific errors from application code
 * - Provide meaningful error messages
 * - Make it easier to handle errors
 * 
 * INHERITANCE:
 * RuntimeException -> StorageException -> Specific exceptions
 * 
 * RuntimeException means: No need to catch with try-catch
 * (but you can if you want to handle errors)
 * 
 * ═══════════════════════════════════════════════════════════════
 */
public class StorageException extends RuntimeException {

    /**
     * Create exception with message
     * 
     * Example:
     *   throw new StorageException("Failed to upload file");
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * Create exception with message and cause
     * 
     * Example:
     *   try {
     *       minioClient.putObject(...);
     *   } catch (MinioException e) {
     *       throw new StorageException("Upload failed", e);
     *   }
     * 
     * The 'cause' is the original exception (MinioException)
     * This preserves the full error stack trace
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
