package com.example.people.management.system.exceptions;

public class ImportConcurencyException extends ImportException {

    public ImportConcurencyException(Long statusId, Long timeoutSeconds) {
        super("Failed to acquire import semaphore within " + timeoutSeconds + " seconds", statusId);
    }
}
