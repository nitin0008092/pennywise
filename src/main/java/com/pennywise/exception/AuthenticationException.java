package com.pennywise.exception;

/**
 * Thrown when user credentials or session validation fails.
 */
public class AuthenticationException extends PennyWiseException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

