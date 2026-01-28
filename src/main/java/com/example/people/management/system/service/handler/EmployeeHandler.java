package com.example.people.management.system.service.handler;

import com.example.people.management.system.dto.EmployeeDto;
import com.example.people.management.system.model.employee.command.EmployeeCommand;
import com.example.people.management.system.factory.PersonFactory;
import com.example.people.management.system.model.employee.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EmployeeHandler implements PersonFactory<Employee, EmployeeDto> {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final ModelMapper modelMapper;

    @Override
    public String getSupportedType() {
        return "EMPLOYEE";
    }

    @Override
    public Employee createPerson(Map<String, Object> data) {
        EmployeeCommand employeeCommand = objectMapper.convertValue(data, EmployeeCommand.class);
        Set<ConstraintViolation<EmployeeCommand>> violations = validator.validate(employeeCommand);
        Employee employee = modelMapper.map(employeeCommand, Employee.class);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("validation error", violations);
        }

        return employee;
    }

    @Override
    public EmployeeDto toDto(Employee emp) {
        EmployeeDto dto = modelMapper.map(emp, EmployeeDto.class);
        dto.setPositionsCount(emp.getPositionsCount());
        dto.setPersonType(getSupportedType());
        return dto;
    }
}
