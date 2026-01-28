package com.example.people.management.system.exceptions;

public class ImportSemaphoreException extends RuntimeException {

    public ImportSemaphoreException(String message) {
        super(message);
    }

    public ImportSemaphoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
