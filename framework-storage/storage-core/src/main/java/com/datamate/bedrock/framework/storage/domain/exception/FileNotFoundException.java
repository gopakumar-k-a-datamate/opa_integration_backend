package com.datamate.bedrock.framework.storage.domain.exception;

/**
 * ═══════════════════════════════════════════════════════════════
 * FILE NOT FOUND EXCEPTION
 * ═══════════════════════════════════════════════════════════════
 * 
 * Thrown when trying to access a file that doesn't exist
 * 
 * WHEN IT'S THROWN:
 * - download("bucket", "nonexistent.pdf") -> FileNotFoundException
 * - getMetadata("bucket", "missing.jpg") -> FileNotFoundException
 * - delete("bucket", "nothere.doc") -> FileNotFoundException
 * 
 * ═══════════════════════════════════════════════════════════════
 */
public class FileNotFoundException extends StorageException {

    /**
     * Create exception with bucket and object key
     * 
     * Example:
     *   throw new FileNotFoundException("restaurant-menus", "rest-123/menu.pdf");
     *   // Message: "File not found: restaurant-menus/rest-123/menu.pdf"
     */
    public FileNotFoundException(String bucketName, String objectKey) {
        super(String.format("File not found: %s/%s", bucketName, objectKey));
    }

    /**
     * Create exception with custom message
     */
    public FileNotFoundException(String message) {
        super(message);
    }
}
