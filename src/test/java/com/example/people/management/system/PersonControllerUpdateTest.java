package com.example.people.management.system;

import com.example.people.management.system.command.CreatePersonCommand;
import com.example.people.management.system.command.UpdatePersonCommand;
import com.example.people.management.system.repository.PersonRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.Map;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PersonControllerUpdateTest extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenUpdateExistingPerson_thenOk() throws Exception {

        CreatePersonCommand createDto = new CreatePersonCommand();
        createDto.setType("EMPLOYEE");
        Map<String, Object> createData = Map.of(
                "firstName", "Anna",
                "lastName", "Nowak",
                "position", "tester",
                "salary", 8000.0,
                "pesel", 88011212345L,
                "employmentStartDate", "2021-05-10",
                "email", "anna.nowak@example.com",
                "height", 165,
                "weight", 60
        );
        createDto.setData(createData);

        MvcResult createResult = mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();


        JsonNode createdNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long personId = createdNode.get("id").asLong();


        UpdatePersonCommand updateDto = UpdatePersonCommand.builder()
                .version(0L)
                .type("EMPLOYEE")
                .build();

        Map<String, Object> updateData = Map.of(
                "firstName", "Ania",
                "lastName", "Nowak",
                "position", "tester",
                "salary", 8500.0,
                "pesel", 88011212345L,
                "employmentStartDate", "2021-05-10",
                "email", "ania.nowak@example.com",
                "height", 165,
                "weight", 60
        );
        updateDto.setData(updateData);

        mockMvc.perform(patch("/api/persons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId))
                .andExpect(jsonPath("$.firstName").value("Ania"))
                .andExpect(jsonPath("$.email").value("ania.nowak@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenUpdateNonExistingPerson_thenNotFound() throws Exception {

        UpdatePersonCommand dto = UpdatePersonCommand.builder()
                .version(0L)
                .type("EMPLOYEE")
                .data(Map.of(
                        "firstName", "Test",
                        "lastName", "User"
                ))
                .build();


        mockMvc.perform(patch("/api/persons/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Person not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenUpdateInvalidData_thenBadRequest() throws Exception {

        CreatePersonCommand c = new CreatePersonCommand();
        c.setType("EMPLOYEE");
        c.setData(Map.of(
                "firstName", "Piotr",
                "lastName", "Zalewski",
                "position", "dev",
                "salary", 10000.0,
                "pesel", 90052554321L,
                "employmentStartDate", "2022-01-01",
                "email", "piotr.zalewski@example.com",
                "height", 175,
                "weight", 70
        ));
        MvcResult r = mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isCreated())
                .andReturn();

        long id = objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();

        UpdatePersonCommand bad = UpdatePersonCommand.builder()
                .version(0L)
                .type("EMPLOYEE")
                .data(Map.of(
                        "firstName", "",
                        "lastName", "Zalewski"
                ))
                .build();

        mockMvc.perform(patch("/api/persons/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.violations").isArray())

                .andExpect(jsonPath(
                        "$.violations[?(@.field=='firstName')].message")
                        .value(Matchers.hasItem("first name must be between 2 and 50 characters long.")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenUpdatingWithStaleVersion_thenConflict() throws Exception {
        CreatePersonCommand createDto = new CreatePersonCommand();
        createDto.setType("STUDENT");
        Map<String, Object> createData = Map.of(
                "firstName", "Jan",
                "lastName", "Kowalski",
                "universityName", "UAM",
                "studyYear", 3,
                "pesel", 99121212345L,
                "email", "jan.kowalski@example.com",
                "height", 180,
                "fieldOfStudy", "it",
                "scholarshipAmount", 100,
                "weight", 80
        );
        createDto.setData(createData);

        MvcResult createResult = mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long personId = createdNode.get("id").asLong();
        long initialVersion = createdNode.get("version").asLong();

        UpdatePersonCommand firstUpdate = UpdatePersonCommand.builder()
                .version(initialVersion)
                .type("STUDENT")
                .data(Map.of("studyYear", 4))
                .build();

        mockMvc.perform(patch("/api/persons/" + personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUpdate)))
                .andExpect(status().isOk());


        UpdatePersonCommand secondUpdateWithStaleVersion = UpdatePersonCommand.builder()
                .version(initialVersion)
                .type("STUDENT")
                .data(Map.of("universityName", "Politechnika"))
                .build();

        mockMvc.perform(patch("/api/persons/" + personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUpdateWithStaleVersion)))
                .andExpect(status().isConflict());
    }
}
