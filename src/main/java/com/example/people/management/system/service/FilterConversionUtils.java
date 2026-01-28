package com.example.people.management.system.service;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@UtilityClass
public class FilterConversionUtils {

    public String asString(Object obj) {
        return (obj instanceof String) ? (String) obj : null;
    }

    public Integer asInteger(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public Double asDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public LocalDate asDate(Object obj) {
        if (obj instanceof String s) {
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}
