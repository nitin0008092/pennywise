package com.pennywise.exception;

/**
 * Thrown when business rules or input parameters fail domain validation.
 */
public class ValidationException extends PennyWiseException {
    public ValidationException(String message) {
        super(message);
    }
}

