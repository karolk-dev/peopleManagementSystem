package com.example.people.management.system.exceptions;

public class CsvCellParsingException extends RuntimeException {

    public CsvCellParsingException(String message, String value, Throwable cause) {
        super(String.format("%s from value '%s'", message, value), cause);
    }


}
