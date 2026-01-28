package com.example.people.management.system.service.importJDBC;

import com.example.people.management.system.config.ImportRedissonConfig;
import com.example.people.management.system.config.MinioProperties;
import com.example.people.management.system.dto.ImportStatusResponseDto;
import com.example.people.management.system.exceptions.FileProcessingException;
import com.example.people.management.system.model.ImportTask;
import com.example.people.management.system.model.StatusInfo;
import com.example.people.management.system.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportRequestService {

    private final StatusService statusService;
    private final ImportRedissonConfig importRedissonConfig;
    private final RedissonClient redissonClient;
    private final S3Client s3Client;
    private final MinioProperties minioProperties;

    public ImportStatusResponseDto handleImportRequest(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileProcessingException("Uploaded file is empty");
        }
        StatusInfo statusInfo = statusService.createInitialStatus();

        try {
            String safeSuffix = sanitizeFilename(file.getOriginalFilename());
            String objectKey = "imports/" + statusInfo.getId() + "-" + safeSuffix;
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(minioProperties.getBucket())
                            .key(objectKey)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            RBlockingQueue<ImportTask> queue = redissonClient.getBlockingQueue(importRedissonConfig.getQueueKey());
            queue.add(new ImportTask(statusInfo.getId(), objectKey));

            return new ImportStatusResponseDto(statusInfo.getId(), "Import started");
        } catch (IOException | S3Exception e) {
            statusService.markAsFailed(statusInfo.getId());
            log.error("Upload to MinIO failed for status {}", statusInfo.getId(), e);
            throw new FileProcessingException("file upload error", e);  
        }
    }

    private String sanitizeFilename(String original) {
        if (original == null) return "upload";
        String name = Paths.get(original).getFileName().toString();
        name = name.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        if (name.length() > 100) name = name.substring(name.length() - 100);
        return name;
    }
}


