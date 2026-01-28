package com.example.people.management.system.controller;

import com.example.people.management.system.dto.ImportStatusResponseDto;
import com.example.people.management.system.dto.StatusInfoDto;
import com.example.people.management.system.service.StatusService;
import com.example.people.management.system.service.importJDBC.ImportRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/persons/imports")
@RequiredArgsConstructor
@Slf4j
public class ImportCsvController {

    private final StatusService statusService;
    private final ImportRequestService importRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'IMPORTER')")
    public ResponseEntity<ImportStatusResponseDto> importCsv(@RequestParam MultipartFile file) {
        return new ResponseEntity<>(importRequestService.handleImportRequest(file), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'IMPORTER')")
    public ResponseEntity<StatusInfoDto> getStatus(@PathVariable Long id) {
        return new ResponseEntity<>(statusService.getStatus(id), HttpStatus.OK);
    }
}
