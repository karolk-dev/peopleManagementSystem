package com.example.people.management.system.exceptions;

public class NotAnEmployeeException extends RuntimeException {
    public NotAnEmployeeException(String message) {
        super(message);
    }
}
