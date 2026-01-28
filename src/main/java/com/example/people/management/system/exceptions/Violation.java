package com.example.people.management.system.exceptions;

import lombok.Value;

@Value
public class Violation {
    private String field;
    private String message;
}
