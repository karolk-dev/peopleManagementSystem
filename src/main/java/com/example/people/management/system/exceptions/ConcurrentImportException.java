package com.example.people.management.system.exceptions;

public class ConcurrentImportException extends RuntimeException {
    public ConcurrentImportException(String message) {
        super(message);
    }
}
