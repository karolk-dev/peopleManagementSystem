package com.example.people.management.system;

import com.example.people.management.system.dto.ImportStatusResponseDto;
import com.example.people.management.system.dto.StatusInfoDto;
import com.example.people.management.system.model.ImportStatus;
import com.example.people.management.system.repository.ImportStatusRepository;
import com.example.people.management.system.repository.PersonRepository;
import com.example.people.management.system.service.StatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.FileCopyUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.awaitility.Awaitility.await;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(statements = {
        "DELETE FROM status_info;",
        "DELETE FROM person;"
})
public class ImportCsvIntegrationTest extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ImportStatusRepository importStatusRepository;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StatusService statusService;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeAll
    static void setupSecurityContextInheritance() {
        SecurityContextHolder.setStrategyName(
                SecurityContextHolder.MODE_INHERITABLETHREADLOCAL
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCsvImportSuccessfully() throws Exception {
        //given
        ClassPathResource resource = new ClassPathResource("personsTest1.csv");
        byte[] fileContent = FileCopyUtils.copyToByteArray(resource.getInputStream());
        // when / then
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/persons/imports")
                                .file("file", fileContent)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.statusId").exists())
                .andExpect(jsonPath("$.message").value("Import started"))
                .andDo(result -> {
                    await().atMost(30, SECONDS).untilAsserted(() -> {
                        mockMvc.perform(get("/api/persons/imports/{id}/status", 1L))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("COMPLETED"))
                                .andExpect(jsonPath("$.processedRows").value(1000));
                    });
                });

        Integer size = personRepository.findAll().size();
        assertThat(size).isEqualTo(1000);

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldQueueImportsWhenLimitReached() throws Exception {
        //given
        ClassPathResource resource = new ClassPathResource("personsTestAsync.csv");
        byte[] fileContent = FileCopyUtils.copyToByteArray(resource.getInputStream());

        ClassPathResource resource2 = new ClassPathResource("personsTestAsync2.csv");
        byte[] fileContent2 = FileCopyUtils.copyToByteArray(resource2.getInputStream());
        //when / then
        MvcResult result1 = mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/persons/imports")
                                .file("file", fileContent)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isAccepted())
                .andReturn();

        MvcResult result2 = mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/persons/imports")
                                .file("file", fileContent2)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isAccepted())
                .andReturn();
        Thread.sleep(5000);
        Long id1 = extractId(result1);
        Long id2 = extractId(result2);
        assertStatus(id1, ImportStatus.RUNNING);
        assertStatus(id2, ImportStatus.PENDING);

        await().atMost(1, MINUTES).until(() ->
                getStatus(id2) == ImportStatus.COMPLETED

        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldRollbackOnImportFailure() throws Exception {
        // given
        ClassPathResource resource = new ClassPathResource("personsTestRollback.csv");
        byte[] fileContent = FileCopyUtils.copyToByteArray(resource.getInputStream());

        // when / then
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/persons/imports")
                                .file("file", fileContent)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isAccepted());

        Integer size = personRepository.findAll().size();
        assertThat(size).isEqualTo(0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailOnEmptyFile() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/persons/imports")
                        .file("file", new byte[0])
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldDeleteTempFileAfterImport() throws Exception {
        // given
        ClassPathResource resource = new ClassPathResource("personsTest1.csv");
        byte[] fileContent = FileCopyUtils.copyToByteArray(resource.getInputStream());

        // when
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/persons/imports")
                                .file("file", fileContent)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isAccepted())
                .andReturn();

        Long statusId = extractId(result);

        // then
        await().atMost(30, SECONDS).untilAsserted(() -> {
            ImportStatus status = getStatus(statusId);
            assertThat(status).isEqualTo(ImportStatus.COMPLETED);
        });


        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        try (Stream<Path> files = Files.list(tmpDir)) {
            boolean hasLeftover = files.anyMatch(p -> p.getFileName().toString().contains("personsTest1"));
            assertThat(hasLeftover).isFalse();
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldFailWhenPersonTypeColumnMissing() throws Exception {
        // given
        ClassPathResource resource = new ClassPathResource("personsMissingPersonType.csv");
        byte[] fileContent = FileCopyUtils.copyToByteArray(resource.getInputStream());

        // when / then
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/persons/imports")
                                .file("file", fileContent)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isAccepted())
                .andReturn();
        Long statusId = extractId(result);

        await().atMost(30, SECONDS).untilAsserted(() -> {
            ImportStatus status = getStatus(statusId);
            assertThat(status).isEqualTo(ImportStatus.FAILED);
        });
    }

    private Long extractId(MvcResult result) throws Exception {
        String responseContent = result.getResponse().getContentAsString();
        ImportStatusResponseDto responseDto = objectMapper.readValue(
                responseContent,
                ImportStatusResponseDto.class
        );
        return responseDto.getStatusId();
    }

    private void assertStatus(Long id, ImportStatus expected) throws Exception {
        mockMvc.perform(get("/api/persons/imports/{id}/status", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(expected.toString()));
    }

    private ImportStatus getStatus(Long id) throws Exception {
        String json = mockMvc.perform(get("/api/persons/imports/{id}/status", id))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(json, StatusInfoDto.class).getStatus();
    }
}
