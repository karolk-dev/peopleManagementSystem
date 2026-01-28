package com.example.people.management.system.factory;

import com.example.people.management.system.model.Person;
import com.example.people.management.system.dto.PersonDto;

import java.util.Map;

public interface PersonUpdateFactory<P extends Person, D extends PersonDto> {

    boolean supports(Person person);

    void updateFields(P existing, Map<String, Object> data);

    D toDto(P person);

}
