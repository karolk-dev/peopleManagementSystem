package com.example.people.management.system.service;

import com.example.people.management.system.command.CreatePersonCommand;
import com.example.people.management.system.command.UpdatePersonCommand;
import com.example.people.management.system.exceptions.VersionConflictException;
import com.example.people.management.system.dto.PersonDto;
import com.example.people.management.system.factory.PersonFactory;
import com.example.people.management.system.factory.PersonUpdateFactory;
import com.example.people.management.system.model.Person;
import com.example.people.management.system.repository.PersonRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService {

    private final List<PersonFactory<? extends Person, ? extends PersonDto>> personHandlers;
    private final PersonRepository personRepository;

    private Map<String, PersonFactory<? extends Person, ? extends PersonDto>> factoryMap;
    private final List<PersonUpdateFactory> personUpdaters;
    private final EntityManager entityManager;

    @PostConstruct
    void initHandlers() {
        factoryMap = personHandlers.stream()
                .collect(Collectors.toMap(PersonFactory::getSupportedType, Function.identity()));
        log.info("registered handlers: " + factoryMap.keySet());
    }

    public PersonDto addPerson(CreatePersonCommand createPersonCommand) {
        String personTypeKey = createPersonCommand.getType().toUpperCase();

        PersonFactory<? extends Person, ? extends PersonDto> factory =
                Optional.ofNullable(factoryMap.get(personTypeKey))
                        .orElseThrow(() -> new UnsupportedOperationException("Unsupported person type: " + createPersonCommand.getType()));
        return processPersonCreation(factory, createPersonCommand.getData());
    }

    @Transactional
    public PersonDto updatePerson(Long id, UpdatePersonCommand command) {

        Person person = personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));
        PersonUpdateFactory<Person, PersonDto> updater = findUpdater(person);
        updater.updateFields(person, command.getData());
        person.setVersion(command.getVersion());
        entityManager.detach(person);

        try {
            person = personRepository.saveAndFlush(person);
            return updater.toDto(person);
        } catch (ObjectOptimisticLockingFailureException ole) {
            throw new VersionConflictException("Version conflict: " + ole.getMessage(), ole);
        }
    }

    private <P extends Person, D extends PersonDto> D processPersonCreation(
            PersonFactory<P, D> factory, Map<String, Object> data) {

        P personEntity = factory.createPerson(data);
        P savedPersonEntity = personRepository.save(personEntity);
        return factory.toDto(savedPersonEntity);
    }

    @SuppressWarnings("unchecked")
    private PersonUpdateFactory<Person, PersonDto> findUpdater(Person person) {
        return (PersonUpdateFactory<Person, PersonDto>) personUpdaters.stream()
                .filter(u -> u.supports(person))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("No updater for type: " + person.getClass()));
    }
}
