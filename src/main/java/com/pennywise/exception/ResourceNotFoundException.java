package com.pennywise.exception;

/**
 * Thrown when a requested record is not found in the persistence store.
 */
public class ResourceNotFoundException extends PennyWiseException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

