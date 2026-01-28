package com.example.people.management.system.exceptions;

public class ImportTimeoutException extends RuntimeException {

    public ImportTimeoutException(String message) {
        super(message);
    }

    public ImportTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
