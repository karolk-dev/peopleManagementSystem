package com.example.people.management.system.controller;

import com.example.people.management.system.command.CreatePersonCommand;
import com.example.people.management.system.dto.PersonDto;
import com.example.people.management.system.command.PersonSearchCriteriaCommand;
import com.example.people.management.system.command.UpdatePersonCommand;
import com.example.people.management.system.service.PersonSearchService;
import com.example.people.management.system.service.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
@Slf4j
public class PersonController {

    private final PersonService personService;
    private final PersonSearchService personSearchService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PersonDto> addPerson(@Valid @RequestBody CreatePersonCommand personCommand) {
        PersonDto dto = personService.addPerson(personCommand);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PersonDto>> searchPersons(@RequestBody PersonSearchCriteriaCommand criteria) {
        return new ResponseEntity<>(personSearchService.search(criteria), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PersonDto> updatePerson(@RequestBody UpdatePersonCommand command, @PathVariable Long id) {
        return new ResponseEntity<>(personService.updatePerson(id, command), HttpStatus.OK);
    }
}
