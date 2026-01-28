package com.example.people.management.system;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.http.*;

import org.springframework.util.MultiValueMap;

@Testcontainers
public class BruteForceIntegrationTest {

    static KeycloakContainer keycloak;

    static {
        keycloak = new KeycloakContainer()
                .withRealmImportFile("realm-export-test.json");
        keycloak.start();
    }


    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/realm-test");
    }

    @Test
    void whenLoginFailsMultipleTimes_thenAccountShouldBeLocked() throws InterruptedException {
        String username = "employee-user";
        String correctPassword = "asdf";
        String wrongPassword = "wrong-password";

        for (int i = 0; i < 3; i++) {
            System.out.printf("Attempt %d with wrong password...%n", i + 1);
            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                attemptLogin(username, wrongPassword);
            });
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        }

        HttpClientErrorException finalException = assertThrows(HttpClientErrorException.class, () -> {
            attemptLogin(username, correctPassword);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, finalException.getStatusCode());

        Thread.sleep(11000);

        ResponseEntity<String> successResponse = attemptLogin(username, correctPassword);
        assertEquals(HttpStatus.OK, successResponse.getStatusCode());
    }

    private ResponseEntity<String> attemptLogin(String username, String password) {
        String tokenUrl = keycloak.getAuthServerUrl() + "/realms/realm-test/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", "person-management");
        map.add("client_secret", "lL3VGtbRbuVbWorBvNtPzwfaLR5qvTm1");
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        return restTemplate.postForEntity(tokenUrl, request, String.class);
    }
}
