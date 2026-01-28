package com.example.people.management.system.model;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

public enum ImportStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    ERROR_INFRA
}
