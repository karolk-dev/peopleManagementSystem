package com.example.people.management.system.service.handler;

import com.example.people.management.system.factory.PersonUpdateFactory;
import com.example.people.management.system.model.employee.Employee;
import com.example.people.management.system.model.Person;
import com.example.people.management.system.dto.EmployeeDto;
import com.example.people.management.system.model.employee.command.EditEmployeeCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EmployeeUpdater implements PersonUpdateFactory<Employee, EmployeeDto> {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final ModelMapper modelMapper;

    @Override
    public boolean supports(Person person) {
        return person instanceof Employee;
    }

    @Override
    public void updateFields(Employee existing, Map<String, Object> data) {


        EditEmployeeCommand editEmployeeCommand = objectMapper.convertValue(data, EditEmployeeCommand.class);

        Set<ConstraintViolation<EditEmployeeCommand>> violations = validator.validate(editEmployeeCommand);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("validation error", violations);
        }

        Optional.ofNullable(editEmployeeCommand.getFirstName()).ifPresent(existing::setFirstName);
        Optional.ofNullable(editEmployeeCommand.getLastName()).ifPresent(existing::setLastName);
        Optional.ofNullable(editEmployeeCommand.getEmail()).ifPresent(existing::setEmail);
        Optional.ofNullable(editEmployeeCommand.getSalary()).ifPresent(existing::setSalary);
        Optional.ofNullable(editEmployeeCommand.getHeight()).ifPresent(existing::setHeight);
        Optional.ofNullable(editEmployeeCommand.getWeight()).ifPresent(existing::setWeight);
        Optional.ofNullable(editEmployeeCommand.getEmploymentStartDate()).ifPresent(existing::setEmploymentStartDate);
        Optional.ofNullable(editEmployeeCommand.getPesel()).ifPresent(existing::setPesel);

    }

    @Override
    public EmployeeDto toDto(Employee person) {
        EmployeeDto dto = modelMapper.map(person, EmployeeDto.class);
        dto.setPositionsCount(person.getPositionsCount());
        return dto;
    }
}
