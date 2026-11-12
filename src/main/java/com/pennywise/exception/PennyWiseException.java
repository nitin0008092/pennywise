package com.pennywise.exception;

/**
 * Root unchecked exception for all domain and system exceptions in PennyWise.
 */
public class PennyWiseException extends RuntimeException {
    public PennyWiseException(String message) {
        super(message);
    }

    public PennyWiseException(String message, Throwable cause) {
        super(message, cause);
    }
}

