package com.example.people.management.system.service.handler;

import com.example.people.management.system.command.PersonSearchCriteriaCommand;
import com.example.people.management.system.factory.PersonSearchStrategy;
import com.example.people.management.system.model.employee.Employee;
import com.example.people.management.system.model.Person;
import com.example.people.management.system.dto.EmployeeDto;
import com.example.people.management.system.model.position.Position;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.people.management.system.service.FilterConversionUtils.*;

@Component
@RequiredArgsConstructor
public class EmployeeSpecificationProvider implements PersonSearchStrategy {

    private final ModelMapper modelMapper;

    @Override
    public boolean shouldApply(PersonSearchCriteriaCommand criteria) {
        Map<String, Object> filters = criteria.getFilters();
        return filters.containsKey("salaryFrom") ||
                filters.containsKey("salaryTo") ||
                filters.containsKey("currentPosition") ||
                filters.containsKey("positionStartDateFrom") ||
                filters.containsKey("positionStartDateTo");
    }

    @Override
    public Class<? extends Person> getPersonType() {
        return Employee.class;
    }

    @Override
    public Specification<Person> buildSpecification(PersonSearchCriteriaCommand criteria) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.type(), getPersonType()));

            Double salaryFrom = asDouble(criteria.getFilters().get("salaryFrom"));
            if (salaryFrom != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("salary"), salaryFrom)
                );
            }

            Double salaryTo = asDouble(criteria.getFilters().get("salaryTo"));
            if (salaryTo != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("salary"), salaryTo)
                );
            }

            String currPos = asString(criteria.getFilters().get("currentPosition"));
            if (currPos != null && !currPos.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("currentPosition")),
                        "%" + currPos.toLowerCase() + "%"
                ));
            }

            LocalDate posFrom = asDate(criteria.getFilters().get("positionStartDateFrom"));
            LocalDate posTo = asDate(criteria.getFilters().get("positionStartDateTo"));
            if (posFrom != null || posTo != null) {

                Subquery<Long> sq = query.subquery(Long.class);
                Root<Position> pos = sq.from(Position.class);
                sq.select(pos.get("employee").get("id"))
                        .where(cb.equal(pos.get("employee").get("id"), root.get("id")))
                        .groupBy(pos.get("employee").get("id"));
                List<Predicate> overlap = new ArrayList<>();

                if (posTo != null) {
                    overlap.add(cb.lessThanOrEqualTo(pos.get("startDate"), posTo));
                }

                if (posFrom != null) {
                    overlap.add(cb.greaterThanOrEqualTo(pos.get("endDate"), posFrom));
                }

                Predicate datePredicate = overlap.size() == 2
                        ? cb.and(overlap.get(0), overlap.get(1))
                        : overlap.get(0);
                sq.where(cb.and(
                        cb.equal(pos.get("employee").get("id"), root.get("id")),
                        datePredicate
                ));
                predicates.add(cb.in(root.get("id")).value(sq));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public EmployeeDto toDto(Person person) {
        return modelMapper.map(person, EmployeeDto.class);
    }
}
