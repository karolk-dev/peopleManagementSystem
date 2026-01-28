package com.example.people.management.system;

import com.example.people.management.system.command.CreatePersonCommand;
import com.example.people.management.system.model.position.CreatePositionCommand;
import com.example.people.management.system.repository.PersonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Sql(
        statements = "TRUNCATE TABLE positions, person RESTART IDENTITY CASCADE",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class PersonControllerAddPositionTest extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAddValidPosition_thenCreated() throws Exception {
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
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        int id = 1;
        CreatePositionCommand cmd = new CreatePositionCommand();
        cmd.setTitle("Developer");
        cmd.setStartDate(LocalDate.of(2021, 1, 1));
        cmd.setEndDate(LocalDate.of(2022, 1, 1));
        cmd.setSalary(5000.0);

        mockMvc.perform(post("/api/employees/{employeeId}/positions", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Developer"))
                .andExpect(jsonPath("$.startDate").value("2021-01-01"))
                .andExpect(jsonPath("$.endDate").value("2022-01-01"))
                .andExpect(jsonPath("$.salary").value(5000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAddOverlappingPosition_thenConflict() throws Exception {
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
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        int id = 1;
        CreatePositionCommand first = new CreatePositionCommand();
        first.setTitle("Developer");
        first.setStartDate(LocalDate.of(2021, 1, 1));
        first.setEndDate(LocalDate.of(2022, 1, 1));
        first.setSalary(5000.0);

        mockMvc.perform(post("/api/employees/{employeeId}/positions", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        CreatePositionCommand overlap = new CreatePositionCommand();
        overlap.setTitle("Senior");
        overlap.setStartDate(LocalDate.of(2021, 6, 1));
        overlap.setEndDate(LocalDate.of(2023, 1, 1));
        overlap.setSalary(7000.0);

        mockMvc.perform(post("/api/employees/{employeeId}/positions", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlap)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Overlapping position exists")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAddPositionInvalidPeriod_thenBadRequest() throws Exception {
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
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
        int id = 1;
        CreatePositionCommand cmd = new CreatePositionCommand();
        cmd.setTitle("Tester");
        cmd.setStartDate(LocalDate.of(2022, 1, 1));
        cmd.setEndDate(LocalDate.of(2021, 12, 31));
        cmd.setSalary(4000.0);

        mockMvc.perform(post("/api/employees/{employeeId}/positions", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString("endDate must be on or after startDate")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAddPositionToNonExistingEmployee_thenNotFound() throws Exception {
        CreatePositionCommand cmd = new CreatePositionCommand();
        cmd.setTitle("DevOps");
        cmd.setStartDate(LocalDate.of(2021, 5, 1));
        cmd.setEndDate(LocalDate.of(2022, 5, 1));
        cmd.setSalary(6000.0);

        mockMvc.perform(post("/api/employees/{employeeId}/positions", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(Matchers.containsString("Employee not found")));
    }
}
