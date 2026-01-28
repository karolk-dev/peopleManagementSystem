package com.example.people.management.system.service.handler;


import com.example.people.management.system.command.PersonSearchCriteriaCommand;
import com.example.people.management.system.factory.PersonSearchStrategy;
import com.example.people.management.system.model.Person;
import com.example.people.management.system.model.student.Student;
import com.example.people.management.system.dto.StudentDto;
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
public class StudentSpecyficationProvider implements PersonSearchStrategy {

    private final ModelMapper modelMapper;

    @Override
    public boolean shouldApply(PersonSearchCriteriaCommand criteria) {
        Map<String, Object> filters = criteria.getFilters();
        return filters.containsKey("universityName") ||
                filters.containsKey("fieldOfStudy") ||
                filters.containsKey("studyYearFrom") ||
                filters.containsKey("studyYearTo") ||
                filters.containsKey("scholarshipFrom") ||
                filters.containsKey("scholarshipTo");
    }

    @Override
    public Class<? extends Person> getPersonType() {
        return Student.class;
    }

    @Override
    public Specification<Person> buildSpecification(PersonSearchCriteriaCommand criteria) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.type(), Student.class));

            String uni = asString(criteria.getFilters().get("universityName"));
            if (uni != null && !uni.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("universityName")),
                        "%" + uni.toLowerCase() + "%"
                ));
            }
            String field = asString(criteria.getFilters().get("fieldOfStudy"));
            if (field != null && !field.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("fieldOfStudy")),
                        "%" + field.toLowerCase() + "%"
                ));
            }
            Integer yearFrom = asInteger(criteria.getFilters().get("studyYearFrom"));
            if (yearFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("studyYear"), yearFrom));
            }
            Integer yearTo = asInteger(criteria.getFilters().get("studyYearTo"));
            if (yearTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("studyYear"), yearTo));
            }
            Double scholarshipFrom = asDouble(criteria.getFilters().get("scholarshipFrom"));
            if (scholarshipFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("scholarshipAmount"), scholarshipFrom));
            }
            Double scholarshipTo = asDouble(criteria.getFilters().get("scholarshipTo"));
            if (scholarshipTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("scholarshipAmount"), scholarshipTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public StudentDto toDto(Person person) {
        return modelMapper.map(person, StudentDto.class);
    }

}
