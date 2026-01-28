package com.example.people.management.system.service.importJDBC;

import com.example.people.management.system.config.ImportRedissonConfig;
import com.example.people.management.system.config.MinioProperties;
import com.example.people.management.system.model.ImportTask;
import com.example.people.management.system.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncImportRunner {

    private final RedissonClient redissonClient;
    private final ImportRedissonConfig importConfig;
    private final CsvImportService csvImportService;
    private final StatusService statusService;
    private final S3Client s3Client;
    private final MinioProperties minioProperties;


    @Async
    public void startConsumer() {
        RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore(importConfig.getKey());
        RBlockingQueue<ImportTask> queue = redissonClient.getBlockingQueue(importConfig.getQueueKey());

        while (!Thread.currentThread().isInterrupted()) {
            String permitId = null;
            ImportTask task = null;
            try {
                permitId = semaphore.tryAcquire(
                        importConfig.getWaitTimeoutSeconds(),
                        importConfig.getLeaseTime(),
                        TimeUnit.SECONDS);
                if (permitId == null) {
                    continue;
                }
                task = queue.take();
                importTask(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Consumer interrupted, exiting loop");
                break;
            } catch (Exception e) {
                if (task != null) {
                    log.error("Unexpected error during import for status {}", task.getImportId(), e);
                    statusService.markAsErrorInfra(task.getImportId());
                } else {
                    log.error("Error in consumer loop (before task acquired)", e);
                }
            } finally {
                if (permitId != null) {
                    try {
                        semaphore.release(permitId);
                    } catch (Exception ex) {
                        log.error("Failed to release semaphore permit {}", permitId, ex);
                    }
                }

            }
        }
    }

    private void importTask(ImportTask task) {
        if (task.getObjectKey() == null) {
            statusService.markAsFailed(task.getImportId());
            return;
        }

        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(minioProperties.getBucket())
                .key(task.getObjectKey())
                .build();

        try (ResponseInputStream<GetObjectResponse> in = s3Client.getObject(getReq)) {
            csvImportService.importFromCsv(in, task.getImportId());
        } catch (IOException e) {
            statusService.markAsErrorInfra(task.getImportId());
        } finally {
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(minioProperties.getBucket())
                        .key(task.getObjectKey())
                        .build());
            } catch (Exception ex) {
                log.warn("Failed to delete object {} in bucket {} for status {}",
                        task.getObjectKey(), minioProperties.getBucket(), task.getImportId(), ex);
            }
        }
    }
}
