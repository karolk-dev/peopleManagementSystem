package com.example.people.management.system.exceptions;

public class CsvImportException extends ImportException {

    public CsvImportException(String message, Long statusId, Throwable cause) {
        super(message, statusId);
        initCause(cause);

    }
}
