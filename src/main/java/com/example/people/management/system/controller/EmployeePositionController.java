package com.example.people.management.system.controller;

import com.example.people.management.system.model.position.CreatePositionCommand;
import com.example.people.management.system.dto.PositionDto;
import com.example.people.management.system.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeePositionController {

    private final PositionService positionService;

    @PostMapping("/{employeeId}/positions")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<PositionDto> addPosition(@PathVariable Long employeeId, @Valid @RequestBody CreatePositionCommand cmd) {
        PositionDto created = positionService.addPosition(employeeId, cmd);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
