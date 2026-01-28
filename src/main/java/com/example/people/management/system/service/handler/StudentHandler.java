package com.example.people.management.system.service.handler;

import com.example.people.management.system.factory.PersonFactory;
import com.example.people.management.system.model.student.Student;
import com.example.people.management.system.dto.StudentDto;
import com.example.people.management.system.model.student.command.StudentCommand;
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
public class StudentHandler implements PersonFactory<Student, StudentDto> {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final ModelMapper modelMapper;

    @Override
    public String getSupportedType() {
        return "STUDENT";
    }

    @Override
    public Student createPerson(Map<String, Object> data) {
        StudentCommand studentCommand = objectMapper.convertValue(data, StudentCommand.class);
        Set<ConstraintViolation<StudentCommand>> violations = validator.validate(studentCommand);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("validation error", violations);
        }
        Student student = modelMapper.map(studentCommand, Student.class);

        return student;
    }

    @Override
    public StudentDto toDto(Student student) {
        StudentDto dto = modelMapper.map(student, StudentDto.class);
        dto.setPersonType(getSupportedType());
        return dto;
    }
}
