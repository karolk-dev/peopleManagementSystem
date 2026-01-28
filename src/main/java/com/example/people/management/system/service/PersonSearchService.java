package com.example.people.management.system.service;

import com.example.people.management.system.command.PersonSearchCriteriaCommand;
import com.example.people.management.system.factory.PersonSearchStrategy;
import com.example.people.management.system.model.Person;
import com.example.people.management.system.dto.PersonDto;
import com.example.people.management.system.repository.PersonRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.example.people.management.system.service.FilterConversionUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonSearchService {

    private final PersonRepository personRepository;

    private final List<PersonSearchStrategy> strategies;

    public Page<PersonDto> search(PersonSearchCriteriaCommand criteria) {
        Specification<Person> spec = buildCombinedSpecification(criteria);
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize());

        Page<Person> result = personRepository.findAll(spec, pageable);
        return result.map(this::mapToDto);

    }

    private Specification<Person> buildCombinedSpecification(PersonSearchCriteriaCommand criteria) {

        Specification<Person> baseSpec = buildBaseSpecification(criteria);

        List<PersonSearchStrategy> activeStrategies = strategies.stream()
                .filter(strategy -> strategy.shouldApply(criteria))
                .toList();

        if (activeStrategies.isEmpty()) {
            return baseSpec;
        }

        if (activeStrategies.size() > 1) {
            return (root, query, cb) -> cb.disjunction();
        }

        PersonSearchStrategy strategy = activeStrategies.getFirst();
        Specification<Person> typeSpec = strategy.buildSpecification(criteria);
        return baseSpec.and(typeSpec);
    }

    private Specification<Person> buildBaseSpecification(PersonSearchCriteriaCommand criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String firstName = asString(criteria.getFilters().get("firstName"));
            if (firstName != null && !firstName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("firstName")),
                        "%" + firstName.toLowerCase() + "%")
                );
            }

            String lastName = asString(criteria.getFilters().get("lastName"));
            if (lastName != null && !lastName.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("lastName")),
                        "%" + lastName.toLowerCase() + "%")
                );
            }

            Integer heightFrom = asInteger(criteria.getFilters().get("heightFrom"));
            if (heightFrom != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("height"), heightFrom)
                );
            }

            Integer heightTo = asInteger(criteria.getFilters().get("heightTo"));
            if (heightTo != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("height"), heightTo)
                );
            }

            String personType = asString(criteria.getFilters().get("personType"));
            if (personType != null && !personType.isBlank()) {
                predicates.add(cb.like(cb.upper(root.get("personType")),
                        "%" + personType.toUpperCase() + "%")
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PersonDto mapToDto(Person person) {
        return strategies.stream()
                .filter(strategy -> strategy.getPersonType().isInstance(person))
                .findFirst()
                .map(strategy -> strategy.toDto(person))
                .orElseThrow(() -> new IllegalStateException("No DTO mapper for type: " + person.getClass()));
    }

}
