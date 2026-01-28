package com.example.people.management.system;

import com.example.people.management.system.command.CreatePersonCommand;
import com.example.people.management.system.command.PersonSearchCriteriaCommand;
import com.example.people.management.system.repository.PersonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Sql(
        statements = "TRUNCATE TABLE person RESTART IDENTITY CASCADE",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class PersonControllerSearchTest extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    private void createEmployee(String firstName, String lastName, String email,
                                double salary, int height, double weight) throws Exception {
        String pesel = String.valueOf((long) (1_000_000_0000L + Math.random() * 9_000_000_0000L));
        CreatePersonCommand dto = new CreatePersonCommand();
        dto.setType("EMPLOYEE");
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("position", "dev");
        data.put("salary", salary);
        data.put("pesel", pesel);
        data.put("employmentStartDate", "2021-01-01");
        data.put("email", email);
        data.put("height", height);
        data.put("weight", weight);
        dto.setData(data);

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    private void createStudent(String firstName, String lastName, String email,
                               String university, int studyYear, double scholarship, String fieldOfStudy,
                               int height, double weight) throws Exception {
        String pesel = String.valueOf((long) (1_000_000_0000L + Math.random() * 9_000_000_0000L));
        CreatePersonCommand dto = new CreatePersonCommand();
        dto.setType("STUDENT");
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("universityName", university);
        data.put("studyYear", studyYear);
        data.put("scholarshipAmount", scholarship);
        data.put("fieldOfStudy", fieldOfStudy);
        data.put("pesel", pesel);
        data.put("email", email);
        data.put("height", height);
        data.put("weight", weight);
        dto.setData(data);

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @BeforeEach
    void setup() throws Exception {
        personRepository.deleteAll();

        createEmployee("Jan", "Kowalski", "jan.k@example.com", 10000, 180, 75);
        createEmployee("Anna", "Nowak", "anna.n@example.com", 12000, 165, 60);
        createEmployee("Alicja", "Wiśniewska", "janina.w@example.com", 15000, 170, 65);
        createEmployee("Karol", "Nowak", "karol.n@example.com", 350.0, 172, 70.0);
        createStudent("Marek", "Kowalczyk", "marek.k@example.com", "Politechnika Warszawska", 2, 1000.0, "it", 185, 80.0);
        createStudent("Ewa", "Nowak", "ewa.n@example.com", "Uniwersytet Warszawski", 3, 1200.0, "it", 160, 55.0);
        createStudent("Jan", "Kowalski", "jan.k2@example.com", "Politechnika Wrocławska", 1, 0.0, "it", 175, 70.0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchPersons_byPersonType_shouldReturnOnlyEmployees() throws Exception {
        PersonSearchCriteriaCommand criteria = new PersonSearchCriteriaCommand();
        criteria.getFilters().put("personType", "EMPLOYEE");

        mockMvc.perform(post("/api/persons/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.content[*].personType", everyItem(is("EMPLOYEE"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchPersons_byPersonType_shouldReturnOnlyStudents() throws Exception {
        PersonSearchCriteriaCommand criteria = new PersonSearchCriteriaCommand();
        criteria.getFilters().put("personType", "STUDENT");

        mockMvc.perform(post("/api/persons/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].personType", everyItem(is("STUDENT"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchPersons_byCommonAttributeWithoutType_shouldReturnAllMatching() throws Exception {
        PersonSearchCriteriaCommand criteria = new PersonSearchCriteriaCommand();
        criteria.getFilters().put("lastName", "Nowak");

        mockMvc.perform(post("/api/persons/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].lastName", everyItem(is("Nowak"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchPersons_bySpecificAttributeWithoutType_shouldReturnMatchingType() throws Exception {
        PersonSearchCriteriaCommand criteria = new PersonSearchCriteriaCommand();
        criteria.getFilters().put("universityName", "Warszawska");

        mockMvc.perform(post("/api/persons/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email", is("marek.k@example.com")))
                .andExpect(jsonPath("$.content[0].personType", is("STUDENT")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchPersons_byTypeAndSpecificAttribute_shouldReturnFiltered() throws Exception {
        PersonSearchCriteriaCommand criteria = new PersonSearchCriteriaCommand();
        criteria.getFilters().put("personType", "STUDENT");
        criteria.getFilters().put("studyYear", 2);
        criteria.getFilters().put("studyYearFrom", 2);
        criteria.getFilters().put("studyYearTo", 2);

        mockMvc.perform(post("/api/persons/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email", is("marek.k@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchPersons_byMultipleSpecificAttributesDifferentTypes_shouldReturnEmpty() throws Exception {
        PersonSearchCriteriaCommand criteria = new PersonSearchCriteriaCommand();
        criteria.getFilters().put("universityName", "Politechnika Warszawska");
        criteria.getFilters().put("salaryFrom", 10000.0);

        mockMvc.perform(post("/api/persons/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchPersons_byHeightRange_shouldReturnMatching() throws Exception {
        PersonSearchCriteriaCommand criteria = new PersonSearchCriteriaCommand();
        criteria.getFilters().put("heightFrom", 170);
        criteria.getFilters().put("heightTo", 175);

        mockMvc.perform(post("/api/persons/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchPersons_byPagination_shouldReturnPage() throws Exception {
        PersonSearchCriteriaCommand criteria = new PersonSearchCriteriaCommand();
        criteria.setPage(0);
        criteria.setSize(3);

        mockMvc.perform(post("/api/persons/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.totalElements", is(7)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchPersons_byNonExistingCriteria_shouldReturnEmpty() throws Exception {
        PersonSearchCriteriaCommand criteria = new PersonSearchCriteriaCommand();
        criteria.getFilters().put("firstName", "NonExistingName");

        mockMvc.perform(post("/api/persons/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchPersons_byPersonTypeAndHeightRange_shouldReturnFiltered() throws Exception {
        PersonSearchCriteriaCommand criteria = new PersonSearchCriteriaCommand();
        criteria.getFilters().put("personType", "EMPLOYEE");
        criteria.getFilters().put("heightFrom", 170);
        criteria.getFilters().put("heightTo", 175);

        mockMvc.perform(post("/api/persons/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].email", containsInAnyOrder(
                        "janina.w@example.com", "karol.n@example.com")));
    }

}
