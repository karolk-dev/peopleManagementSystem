package com.example.people.management.system.service.importJDBC;

import java.util.Map;
import java.util.function.Function;

public record ColumnMapping(
        String csvColumn,
        Function<Map<String, String>, Object> valueExtractor,
        int sqlType,
        boolean required) {
}
