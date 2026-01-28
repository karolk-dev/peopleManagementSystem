package com.example.people.management.system.service.importJDBC;

import com.example.people.management.system.exceptions.CsvCellParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
@Slf4j
public class CsvParsingUtils {

    private CsvParsingUtils() {
    }

    public String parseString(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }


    public Integer parseInt(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new CsvCellParsingException("Failed to parse INT", value, e);
        }
    }

    public Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new CsvCellParsingException("Failed to parse DOUBLE", value, e);
        }
    }

    public LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new CsvCellParsingException("Failed to parse DATE", value, e);
        }
    }
}
