package com.example.people.management.system.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class ImportException extends RuntimeException {
    private final Long statusId;


    public ImportException(String message, Long statusId) {
        super(message);
        this.statusId = statusId;
    }
}
