package com.example.people.management.system.service.handler;

import com.example.people.management.system.factory.PersonUpdateFactory;
import com.example.people.management.system.model.Person;
import com.example.people.management.system.model.student.Student;
import com.example.people.management.system.dto.StudentDto;
import com.example.people.management.system.model.student.command.EditStudentCommand;
import com.example.people.management.system.model.student.command.StudentCommand;
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
public class StudentUpdater implements PersonUpdateFactory<Student, StudentDto> {

    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Override
    public boolean supports(Person person) {
        return person instanceof Student;
    }

    @Override
    public void updateFields(Student existing, Map<String, Object> data) {

        EditStudentCommand cmd = objectMapper.convertValue(data, EditStudentCommand.class);


        Set<ConstraintViolation<EditStudentCommand>> violations = validator.validate(cmd);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("validation error", violations);
        }

        Optional.ofNullable(cmd.getFirstName()).ifPresent(existing::setFirstName);
        Optional.ofNullable(cmd.getLastName()).ifPresent(existing::setLastName);
        Optional.ofNullable(cmd.getEmail()).ifPresent(existing::setEmail);
        Optional.ofNullable(cmd.getHeight()).ifPresent(existing::setHeight);
        Optional.ofNullable(cmd.getWeight()).ifPresent(existing::setWeight);

        Optional.ofNullable(cmd.getUniversityName()).ifPresent(existing::setUniversityName);
        Optional.ofNullable(cmd.getFieldOfStudy()).ifPresent(existing::setFieldOfStudy);
        Optional.ofNullable(cmd.getStudyYear()).ifPresent(existing::setStudyYear);
        Optional.ofNullable(cmd.getScholarshipAmount()).ifPresent(existing::setScholarshipAmount);
    }

    @Override
    public StudentDto toDto(Student person) {
        return modelMapper.map(person, StudentDto.class);
    }
}
