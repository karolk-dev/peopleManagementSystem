package com.example.people.management.system.service.importJDBC;

import com.example.people.management.system.config.CsvImportConfig;
import com.example.people.management.system.exceptions.CsvImportException;
import com.example.people.management.system.service.StatusService;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {

    private final JdbcTemplate jdbcTemplate;
    private final CsvImportStrategyFactory strategyFactory;
    private final StatusService statusService;
    private final CsvImportConfig config;

    @Transactional
    public void importFromCsv(InputStream inputStream, Long statusId) {
        statusService.markAsRunning(statusId);

        Map<String, List<Object[]>> batchArgs = new HashMap<>();

        try (var reader = new CSVReaderHeaderAware(new InputStreamReader(inputStream))) {
            Map<String, String> csvRow;
            long totalProcessed = 0;

            while ((csvRow = reader.readMap()) != null) {
                String personType = csvRow.get(config.getPersonTypeColumn());

                if (personType == null || personType.isBlank()) {
                    log.error("Missing personType in CSV row: {}", csvRow);
                    statusService.markAsFailed(statusId);
                    return;
                }

                CsvImportStrategy strategy = strategyFactory.getStrategy(personType)
                        .orElseThrow(() -> new IllegalArgumentException("No import strategy found for type: " + personType));

                batchArgs.computeIfAbsent(personType, k -> new ArrayList<>());
                var params = strategy.createSqlParameters(csvRow);
                batchArgs.get(personType).add(params);
                totalProcessed++;

                if (batchArgs.get(personType).size() >= config.getBatchSize()) {
                    executeBatch(personType, strategy.getInsertSql(), batchArgs.get(personType), statusId);
                }
            }

            flushRemainingBatches(batchArgs, statusId);
            log.info("Successfully imported {} records.", totalProcessed);
            statusService.markAsCompleted(statusId, totalProcessed);

        } catch (IOException | CsvValidationException e) {
            statusService.markAsFailed(statusId);
            throw new CsvImportException("Csv parse error", statusId, e);
        } catch (Exception e) {
            statusService.markAsFailed(statusId);
            throw new CsvImportException("CSV import failed for job ", statusId, e);
        }
    }

    private void executeBatch(String personType, String sql, List<Object[]> args, Long statusId) {
        if (!args.isEmpty()) {
            try {
                int[] columnTypes = strategyFactory.getStrategy(personType)
                        .orElseThrow(() -> new CsvImportException("No import strategy for type: " + personType, statusId, null))
                        .getColumnTypes();
                jdbcTemplate.batchUpdate(sql, args, columnTypes);
                statusService.updateProgress(statusId, args.size());
                args.clear();
            } catch (Exception e) {
                log.error("Error executing batch for type {} and status {}: {}", personType, statusId, e.getMessage(), e);
                statusService.markAsErrorInfra(statusId);
                throw e;
            }
        }
    }

    private void flushRemainingBatches(Map<String, List<Object[]>> batchArgs, Long statusId) {
        batchArgs.forEach((type, args) -> {
            CsvImportStrategy strategy = strategyFactory.getStrategy(type)
                    .orElseThrow(() -> new CsvImportException("No import strategy for type: " + type, statusId, null));
            executeBatch(type, strategy.getInsertSql(), args, statusId);
        });
    }
}
