package com.example.people.management.system.exceptions;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ValidationErrorResponse {
    @Singular
    List<Violation> violations;
}
