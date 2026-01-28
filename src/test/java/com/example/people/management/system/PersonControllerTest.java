package com.example.people.management.system;

import com.example.people.management.system.command.CreatePersonCommand;
import com.example.people.management.system.model.Person;
import com.example.people.management.system.dto.PersonDto;
import com.example.people.management.system.repository.PersonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
public class PersonControllerTest extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPerson_shouldReturnCreatedPersonDto_andPersistInDatabase() throws Exception {
        CreatePersonCommand dto = new CreatePersonCommand();
        dto.setType("EMPLOYEE");
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", "Jan");
        data.put("lastName", "Kowalski");
        data.put("position", "java developer");
        data.put("salary", 15000.0);
        data.put("pesel", 22091819962L);
        data.put("employmentStartDate", "2022-08-15");
        data.put("email", "jan.kowalski@example.com");
        data.put("height", 180);
        data.put("weight", 75);
        dto.setData(data);

        String json = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is("Jan")))
                .andExpect(jsonPath("$.lastName", is("Kowalski")))
                .andExpect(jsonPath("$.email", is("jan.kowalski@example.com")))
                .andExpect(jsonPath("$.height", is(180)))
                .andExpect(jsonPath("$.weight", is(75.0)))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        PersonDto created = objectMapper.readValue(responseJson, PersonDto.class);

        Optional<Person> fromDb = personRepository.findById(created.getId());
        assertThat(fromDb).isPresent();
        Person personEntity = fromDb.get();
        assertThat(personEntity.getFirstName()).isEqualTo("Jan");
        assertThat(personEntity.getLastName()).isEqualTo("Kowalski");
        assertThat(personEntity.getEmail()).isEqualTo("jan.kowalski@example.com");
        assertThat(personEntity.getHeight()).isEqualTo(180);
        assertThat(personEntity.getWeight()).isEqualTo(75.0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPerson_withInvalidData_shouldReturnValidationErrors() throws Exception {
        CreatePersonCommand dto = new CreatePersonCommand();
        dto.setType("EMPLOYEE");
        dto.setData(new HashMap<>());

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.violations[*].field", hasItems(
                        "firstName", "lastName", "email", "salary", "height", "weight", "position", "pesel", "employmentStartDate"
                )))
                .andExpect(jsonPath("$.violations[*].message", hasItem("first name is required")));
    }
}
