package com.example.people.management.system.service.importJDBC.strategy;

import com.example.people.management.system.service.importJDBC.ColumnMapping;
import com.example.people.management.system.service.importJDBC.CsvImportStrategy;
import com.example.people.management.system.service.importJDBC.CsvParsingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmployeeImportStrategy implements CsvImportStrategy {

    private static final String TYPE = "EMPLOYEE";
    private final CsvParsingUtils valueParser;

    @Override
    public String getSupportedType() {
        return TYPE;
    }

    @Override
    public String getTableName() {
        return "person";
    }

    @Override
    public Map<String, ColumnMapping> getColumnMappings() {
        Map<String, ColumnMapping> mappings = new LinkedHashMap<>();

        mappings.put("person_type", new ColumnMapping(
                "person_type",
                row -> TYPE,
                Types.VARCHAR,
                true
        ));

        mappings.put("first_name", new ColumnMapping(
                "first_name",
                row -> row.get("first_name"),
                Types.VARCHAR,
                true
        ));

        mappings.put("last_name", new ColumnMapping(
                "last_name",
                row -> row.get("last_name"),
                Types.VARCHAR,
                true
        ));

        mappings.put("pesel", new ColumnMapping(
                "pesel",
                row -> valueParser.parseString(row.get("pesel")),
                Types.VARCHAR,
                true
        ));

        mappings.put("height", new ColumnMapping(
                "height",
                row -> valueParser.parseInt(row.get("height")),
                Types.INTEGER,
                false
        ));

        mappings.put("weight", new ColumnMapping(
                "weight",
                row -> valueParser.parseDouble(row.get("weight")),
                Types.DOUBLE,
                false
        ));

        mappings.put("email", new ColumnMapping(
                "email",
                row -> valueParser.parseString(row.get("email")),
                Types.VARCHAR,
                false
        ));

        mappings.put("employment_start_date", new ColumnMapping(
                "employment_start_date",
                row -> valueParser.parseDate(row.get("employment_start_date")),
                Types.DATE,
                true
        ));

        mappings.put("current_position", new ColumnMapping(
                "current_position",
                row -> valueParser.parseString(row.get("current_position")),
                Types.VARCHAR,
                false
        ));

        mappings.put("salary", new ColumnMapping(
                "salary",
                row -> valueParser.parseDouble(row.get("salary")),
                Types.DOUBLE,
                false
        ));

        return mappings;
    }
}
