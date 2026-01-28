package com.example.people.management.system.service;

import com.example.people.management.system.exceptions.StatusNotFoundException;
import com.example.people.management.system.model.ImportStatus;
import com.example.people.management.system.model.StatusInfo;
import com.example.people.management.system.dto.StatusInfoDto;
import com.example.people.management.system.repository.ImportStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusService {

    private final ImportStatusRepository statusRepository;
    private final ModelMapper modelMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StatusInfo createInitialStatus() {
        StatusInfo statusInfo = StatusInfo.builder()
                .status(ImportStatus.PENDING)
                .build();
        return statusRepository.save(statusInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsRunning(Long id) {
        log.info("Marking import {} as RUNNING", id);
        statusRepository.findById(id).ifPresent(status -> {
            status.setStatus(ImportStatus.RUNNING);
            status.setStartedAt(LocalDateTime.now());
            statusRepository.save(status);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProgress(Long id, int rowsProcessedInBatch) {
        statusRepository.findById(id).ifPresent(status -> {
            status.setProcessedRows(status.getProcessedRows() + rowsProcessedInBatch);
            log.info("update progress = " + id + " " + rowsProcessedInBatch);
            statusRepository.save(status);
            statusRepository.flush();

        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsCompleted(Long id, Long processedRows) {
        log.info("Marking import {} as COMPLETED", id);
        statusRepository.findById(id).ifPresent(status -> {
            status.setStatus(ImportStatus.COMPLETED);
            status.setFinishedAt(LocalDateTime.now());
            status.setProcessedRows(processedRows);
            statusRepository.save(status);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(Long id) {
        statusRepository.findById(id).ifPresent(status -> {
            status.setStatus(ImportStatus.FAILED);
            status.setFinishedAt(LocalDateTime.now());
            statusRepository.save(status);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsErrorInfra(Long id) {
        statusRepository.findById(id).ifPresent(status -> {
            status.setStatus(ImportStatus.ERROR_INFRA);
            status.setFinishedAt(LocalDateTime.now());
            statusRepository.save(status);
        });
    }

    @Transactional(readOnly = true)
    public StatusInfoDto getStatus(Long id) {
        StatusInfo statusInfo = statusRepository.findById(id).orElseThrow(() -> new StatusNotFoundException("Status with id" +
                id + "not found"));
        return modelMapper.map(statusInfo, StatusInfoDto.class);
    }
}
