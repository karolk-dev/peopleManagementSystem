package com.example.people.management.system.service;

import com.example.people.management.system.exceptions.InvalidPositionPeriodException;
import com.example.people.management.system.exceptions.PositionOverlapException;
import com.example.people.management.system.model.Person;
import com.example.people.management.system.model.employee.Employee;
import com.example.people.management.system.model.position.CreatePositionCommand;
import com.example.people.management.system.model.position.Position;
import com.example.people.management.system.dto.PositionDto;
import com.example.people.management.system.repository.PersonRepository;
import com.example.people.management.system.repository.PositionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PositionService {
    private final PersonRepository personRepository;
    private final PositionRepository positionRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public PositionDto addPosition(Long employeeId, CreatePositionCommand cmd) {
        Person person = personRepository.findWithLockById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        if (!(person instanceof Employee emp)) {
            throw new IllegalArgumentException("Person is not an Employee");
        }

        if (cmd.getEndDate().isBefore(cmd.getStartDate())) {
            throw new InvalidPositionPeriodException("endDate must be on or after startDate");
        }

        if (positionRepository.existsOverlapping(employeeId, cmd.getStartDate(), cmd.getEndDate())) {
            throw new PositionOverlapException("Overlapping position exists");
        }

        Position pos = Position.builder()
                .title(cmd.getTitle())
                .startDate(cmd.getStartDate())
                .endDate(cmd.getEndDate())
                .salary(cmd.getSalary())
                .build();
        emp.addPosition(pos);
        emp.setCurrentPosition(cmd.getTitle());
        emp.setSalary(cmd.getSalary());

        Position savedPos = positionRepository.save(pos);

        return modelMapper.map(savedPos, PositionDto.class);
    }
}
