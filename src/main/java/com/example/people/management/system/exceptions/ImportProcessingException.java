package com.example.people.management.system.exceptions;

public class ImportProcessingException extends RuntimeException {

    public ImportProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    public ImportProcessingException(String message) {
        super(message);
    }

}
