package com.example.people.management.system.service.handler;

import com.example.people.management.system.factory.PersonFactory;
import com.example.people.management.system.model.retiree.Retiree;
import com.example.people.management.system.dto.RetireeDto;
import com.example.people.management.system.model.retiree.command.RetireeCommand;
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
public class RetireeHandler implements PersonFactory<Retiree, RetireeDto> {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final ModelMapper modelMapper;

    @Override
    public String getSupportedType() {
        return "RETIREE";
    }


    @Override
    public Retiree createPerson(Map<String, Object> data) {
        RetireeCommand retireeCommand = objectMapper.convertValue(data, RetireeCommand.class);
        Set<ConstraintViolation<RetireeCommand>> violations = validator.validate(retireeCommand);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("validation error", violations);
        }
        Retiree retiree = modelMapper.map(retireeCommand, Retiree.class);
        return retiree;

    }

    @Override
    public RetireeDto toDto(Retiree retiree) {
        RetireeDto dto = modelMapper.map(retiree, RetireeDto.class);
        dto.setPersonType(getSupportedType());
        return dto;
    }
}
