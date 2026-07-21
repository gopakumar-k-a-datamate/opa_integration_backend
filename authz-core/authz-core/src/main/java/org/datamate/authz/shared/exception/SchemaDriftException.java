package org.datamate.authz.shared.exception;

/**
 * Exception thrown during startup when the database schema for authorization policies 
 * does not match the Java annotations (@PolicyResource, @PolicyField).
 */
public class SchemaDriftException extends RuntimeException {
    public SchemaDriftException(String message) {
        super(message);
    }
}
