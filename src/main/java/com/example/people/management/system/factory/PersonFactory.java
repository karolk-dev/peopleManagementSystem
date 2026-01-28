package com.example.people.management.system.factory;

import com.example.people.management.system.dto.PersonDto;
import com.example.people.management.system.model.Person;

import java.util.Map;

public interface PersonFactory<P extends Person, D extends PersonDto> {

    String getSupportedType();

    P createPerson(Map<String, Object> data);

    D toDto(P person);
}
