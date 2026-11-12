package com.pennywise.exception;

/**
 * Thrown when a database or JDBC operation fails.
 */
public class DatabaseException extends PennyWiseException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

