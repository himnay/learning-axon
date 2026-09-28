package com.learning.axon.command.integration;

import com.learning.axon.command.projection.AccountActivityProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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

    @Autowired
    private AccountActivityProjection projection;

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

    @Test
    @DisplayName("event history stays complete after a snapshot is taken")
    void eventHistory_isComplete_afterSnapshot() {
        String accountId = createAccount(500.0, "EUR");   // AccountCreatedEvent + AccountActivatedEvent
        credit(accountId, 10.0);                         // 3rd event: snapshot threshold (3) reached
        credit(accountId, 20.0);

        List<Map<String, Object>> events = client.get().uri("/bank-accounts/{id}/events", accountId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(events).hasSize(4);
        assertThat(events).filteredOn(e -> e.containsKey("creditAmount"))
                .extracting(e -> e.get("creditAmount"))
                .containsExactly(10.0, 20.0);
    }

    @Test
    @DisplayName("replay rebuilds the tracking projection from the event store without double counting")
    void replay_rebuildsProjection() {
        String accountId = createAccount(500.0, "EUR");   // 2 events
        credit(accountId, 5.0);                          // 3 events
        await().atMost(Duration.ofSeconds(10)).until(() -> projection.eventCount(accountId) == 3);

        ResponseEntity<String> replay = client.post().uri("/bank-accounts/replay").retrieve().toEntity(String.class);
        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.OK);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Map<String, Object> status = client.get().uri("/bank-accounts/status")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            assertThat(status).isNotEmpty();
            assertThat(activity(accountId)).containsEntry("events", 3);
        });
        assertThat(projection.eventCount(accountId)).isEqualTo(3);
    }

    @Test
    @DisplayName("debit or credit on an unknown account is rejected with 404")
    void moneyCommand_onUnknownAccount_shouldReturn404() {
        HttpStatus debit = client.put().uri("/bank-accounts/debits/{id}", "no-such-account")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("debitAmount", 5.0, "currency", "EUR"))
                .exchange((req, res) -> HttpStatus.valueOf(res.getStatusCode().value()));
        HttpStatus credit = client.put().uri("/bank-accounts/credits/{id}", "no-such-account")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("creditAmount", 5.0, "currency", "EUR"))
                .exchange((req, res) -> HttpStatus.valueOf(res.getStatusCode().value()));

        assertThat(debit).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(credit).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String createAccount(double balance, String currency) {
        return client.post().uri("/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("startingBalance", balance, "currency", currency))
                .retrieve()
                .body(String.class);
    }

    private void credit(String accountId, double amount) {
        client.put().uri("/bank-accounts/credits/{id}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("creditAmount", amount, "currency", "EUR"))
                .retrieve()
                .toBodilessEntity();
    }

    private Map<String, Object> activity(String accountId) {
        return client.get().uri("/bank-accounts/{id}/activity", accountId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
