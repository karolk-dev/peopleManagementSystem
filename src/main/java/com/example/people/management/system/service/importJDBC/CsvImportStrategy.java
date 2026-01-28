package com.example.people.management.system.service.importJDBC;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface CsvImportStrategy {

    String getSupportedType();

    String getTableName();

    Map<String, ColumnMapping> getColumnMappings();

    default String getInsertSql() {
        Set<String> columns = getColumnMappings().keySet();
        String columnNames = String.join(", ", columns);
        String placeholders = columns.stream()
                .map(c -> "?")
                .collect(Collectors.joining(", "));

        return "INSERT INTO " + getTableName() + " (" + columnNames + ") VALUES (" + placeholders + ")";
    }

    default int[] getColumnTypes() {
        return getColumnMappings().values().stream()
                .mapToInt(ColumnMapping::sqlType)
                .toArray();
    }

    default Object[] createSqlParameters(Map<String, String> csvRow) {
        return getColumnMappings().values().stream()
                .map(mapping -> mapping.valueExtractor().apply(csvRow))
                .toArray();
    }
}
