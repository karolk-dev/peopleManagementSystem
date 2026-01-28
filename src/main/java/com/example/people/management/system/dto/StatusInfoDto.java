package com.example.people.management.system.dto;

import com.example.people.management.system.model.ImportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusInfoDto {

    private Long id;
    private ImportStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long processedRows;
}
