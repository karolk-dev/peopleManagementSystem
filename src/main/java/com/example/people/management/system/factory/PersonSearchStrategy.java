package com.example.people.management.system.factory;

import com.example.people.management.system.command.PersonSearchCriteriaCommand;
import com.example.people.management.system.model.Person;
import com.example.people.management.system.dto.PersonDto;
import org.springframework.data.jpa.domain.Specification;

public interface PersonSearchStrategy {

    boolean shouldApply(PersonSearchCriteriaCommand criteria);

    Class<? extends Person> getPersonType();

    Specification<Person> buildSpecification(PersonSearchCriteriaCommand criteria);

    PersonDto toDto(Person person);
}
