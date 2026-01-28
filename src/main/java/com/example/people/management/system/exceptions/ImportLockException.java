package com.example.people.management.system.exceptions;

public class ImportLockException extends RuntimeException {

    public ImportLockException(String message) {
        super(message);
    }
}
