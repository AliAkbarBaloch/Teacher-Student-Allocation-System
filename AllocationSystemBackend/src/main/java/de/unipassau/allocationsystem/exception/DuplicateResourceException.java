package de.unipassau.allocationsystem.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 */
public class DuplicateResourceException extends RuntimeException {
    /**
     * Constructs a new DuplicateResourceException with the specified detail message.
     * 
     * @param message the detail message explaining which resource is duplicated
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}