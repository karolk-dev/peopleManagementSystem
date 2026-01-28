package com.example.people.management.system.service.handler;

import com.example.people.management.system.factory.PersonUpdateFactory;
import com.example.people.management.system.model.Person;
import com.example.people.management.system.model.retiree.Retiree;
import com.example.people.management.system.dto.RetireeDto;
import com.example.people.management.system.model.retiree.command.EditRetireeCommand;
import com.example.people.management.system.model.retiree.command.RetireeCommand;
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
public class RetireeUpdater implements PersonUpdateFactory<Retiree, RetireeDto> {

    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Override
    public boolean supports(Person person) {
        return person instanceof Retiree;
    }

    @Override
    public void updateFields(Retiree existing, Map<String, Object> data) {

        EditRetireeCommand cmd = objectMapper.convertValue(data, EditRetireeCommand.class);

        Set<ConstraintViolation<EditRetireeCommand>> violations = validator.validate(cmd);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("validation error", violations);
        }
        Optional.ofNullable(cmd.getFirstName()).ifPresent(existing::setFirstName);
        Optional.ofNullable(cmd.getLastName()).ifPresent(existing::setLastName);
        Optional.ofNullable(cmd.getEmail()).ifPresent(existing::setEmail);
        Optional.ofNullable(cmd.getHeight()).ifPresent(existing::setHeight);
        Optional.ofNullable(cmd.getWeight()).ifPresent(existing::setWeight);

        Optional.ofNullable(cmd.getPensionAmount()).ifPresent(existing::setPensionAmount);
        Optional.ofNullable(cmd.getYearsWorked()).ifPresent(existing::setYearsWorked);

    }

    @Override
    public RetireeDto toDto(Retiree person) {
        return modelMapper.map(person, RetireeDto.class);
    }
}
