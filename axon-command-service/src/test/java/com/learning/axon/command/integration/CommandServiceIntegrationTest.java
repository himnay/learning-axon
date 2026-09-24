package com.learning.axon.command.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test of the command side over HTTP: Controller → CommandGateway → event-sourced
 * aggregate → embedded (JPA/H2) event store. RabbitMQ is excluded in the test profile, so the
 * AMQP publisher is not exercised here.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Command Service Integration Tests")
class CommandServiceIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.builder().baseUrl("http://localhost:" + port).build();
    }

    @Test
    @DisplayName("POST /bank-accounts creates an account and returns 201 with its id")
    void createAccount_shouldReturn201() {
        ResponseEntity<String> response = client.post().uri("/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("startingBalance", 100.0, "currency", "EUR"))
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotBlank();
    }

    @Test
    @DisplayName("created account has an AccountCreatedEvent in the event store")
    void createdAccount_isEventSourced() {
        String accountId = client.post().uri("/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("startingBalance", 50.0, "currency", "USD"))
                .retrieve()
                .body(String.class);

        List<Map<String, Object>> events = client.get().uri("/bank-accounts/{id}/events", accountId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(events).isNotEmpty();
        assertThat(events.getFirst()).containsEntry("id", accountId);
    }

    @Test
    @DisplayName("invalid request (blank currency) is rejected with 400")
    void createAccount_withBlankCurrency_shouldReturn400() {
        HttpStatus status = client.post().uri("/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("startingBalance", 10.0, "currency", ""))
                .exchange((req, res) -> HttpStatus.valueOf(res.getStatusCode().value()));

        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
