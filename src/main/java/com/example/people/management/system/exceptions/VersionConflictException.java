package com.example.people.management.system.exceptions;

public class VersionConflictException extends RuntimeException {
    public VersionConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
