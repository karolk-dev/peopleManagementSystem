package com.example.people.management.system.service.handler;

import com.example.people.management.system.command.PersonSearchCriteriaCommand;
import com.example.people.management.system.factory.PersonSearchStrategy;
import com.example.people.management.system.model.Person;
import com.example.people.management.system.model.employee.Employee;
import com.example.people.management.system.model.retiree.Retiree;
import com.example.people.management.system.dto.RetireeDto;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.people.management.system.service.FilterConversionUtils.*;

@Component
@RequiredArgsConstructor
public class RetireeSpecyficationProvider implements PersonSearchStrategy {

    private final ModelMapper modelMapper;

    @Override
    public boolean shouldApply(PersonSearchCriteriaCommand criteria) {
        Map<String, Object> filters = criteria.getFilters();
        return filters.containsKey("pensionFrom") ||
                filters.containsKey("pensionTo") ||
                filters.containsKey("yearsWorkedFrom") ||
                filters.containsKey("yearsWorkedTo");
    }

    @Override
    public Class<? extends Person> getPersonType() {
        return Retiree.class;
    }

    @Override
    public Specification<Person> buildSpecification(PersonSearchCriteriaCommand criteria) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.type(), Employee.class));

            Double pensionFrom = asDouble(criteria.getFilters().get("pensionFrom"));
            if (pensionFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pensionAmount"), pensionFrom));
            }

            Double pensionTo = asDouble(criteria.getFilters().get("pensionTo"));
            if (pensionTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pensionAmount"), pensionTo));
            }

            Integer yearsFrom = asInteger(criteria.getFilters().get("yearsWorkedFrom"));
            if (yearsFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("yearsWorked"), yearsFrom));
            }

            Integer yearsTo = asInteger(criteria.getFilters().get("yearsWorkedTo"));
            if (yearsTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("yearsWorked"), yearsTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));

        };
    }

    @Override
    public RetireeDto toDto(Person person) {
        return modelMapper.map(person, RetireeDto.class);
    }

}
