package org.datamate.collaboration.exception;

/**
 * Global base exception for the entire Collaboration Microservice.
 * Inherits from Bedrock's exception handling system.
 */
public class CollaborationBaseException extends RuntimeException {
    
    // Note: Change 'RuntimeException' to the specific Bedrock Framework base class.
    
    public CollaborationBaseException(String message) {
        super(message);
    }
}
