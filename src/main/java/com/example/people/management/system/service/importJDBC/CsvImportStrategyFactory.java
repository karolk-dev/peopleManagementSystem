package com.example.people.management.system.service.importJDBC;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CsvImportStrategyFactory {

    private final List<CsvImportStrategy> strategies;
    private Map<String, CsvImportStrategy> strategyMap;

    @PostConstruct
    void init() {

        strategyMap = strategies.stream()
                .collect(Collectors.toMap(CsvImportStrategy::getSupportedType, Function.identity()));
    }

    public Optional<CsvImportStrategy> getStrategy(String type) {
        return Optional.ofNullable(strategyMap.get(type));
    }
}
