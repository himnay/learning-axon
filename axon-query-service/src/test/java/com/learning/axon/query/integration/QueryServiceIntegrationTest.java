package com.learning.axon.query.integration;

import com.learning.axon.query.entity.AccountEntity;
import com.learning.axon.query.handler.AccountEventHandler;
import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.AccountCreatedEvent;
import com.learning.axon.shared.events.MoneyCreditedEvent;
import com.learning.axon.shared.events.MoneyDebitedEvent;
import com.learning.axon.shared.notifiers.MoneyDebitNotifier;
import com.learning.axon.shared.queries.AccountDetailsQuery;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the full service against the test profile (H2 read model, RabbitMQ excluded). Events are
 * handed straight to {@link AccountEventHandler}, standing in for the AMQP message source, so the
 * read model, the query handlers and the subscription-query updates run for real.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Query Service Integration Tests")
class QueryServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AccountEventHandler eventHandler;

    @Autowired
    private QueryGateway queryGateway;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.builder().baseUrl("http://localhost:" + port).build();
    }

    @Test
    @DisplayName("unknown account is 404 on the direct JPA and the point-to-point query endpoints")
    void unknownAccount_shouldReturn404() {
        assertThat(statusOf("/bank-accounts/{id}", "no-such-account")).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(statusOf("/bank-accounts/{id}/details", "no-such-account")).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("projected account is served by the point-to-point query")
    void projectedAccount_isQueryable() {
        String id = openAccount(250.0);

        Map<String, Object> account = client.get().uri("/bank-accounts/{id}/details", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(account).containsEntry("id", id).containsEntry("accountBalance", 250.0);
    }

    @Test
    @DisplayName("credit subscription starts with the subscribed account only and then streams credits")
    void creditSubscription_streamsUpdates() {
        String id = openAccount(100.0);
        openAccount(999.0); // another account must not leak into the initial result

        SubscriptionQueryResult<List<AccountEntity>, AccountEntity> result = queryGateway.subscriptionQuery(
                new AccountDetailsQuery(id, 0, 10),
                ResponseTypes.multipleInstancesOf(AccountEntity.class),
                ResponseTypes.instanceOf(AccountEntity.class));
        try {
            StepVerifier.create(result.initialResult())
                    .assertNext(initial -> assertThat(initial).extracting(AccountEntity::getId).containsExactly(id))
                    .verifyComplete();
            StepVerifier.create(result.updates())
                    .then(() -> eventHandler.on(new MoneyCreditedEvent(id, 50.0, "EUR")))
                    .assertNext(update -> assertThat(update.getAccountBalance()).isEqualTo(150.0))
                    .thenCancel()
                    .verify(Duration.ofSeconds(5));
        } finally {
            result.close();
        }
    }

    @Test
    @DisplayName("debit subscription streams an update when money is debited")
    void debitSubscription_streamsUpdates() {
        String id = openAccount(100.0);

        SubscriptionQueryResult<AccountEntity, AccountEntity> result = queryGateway.subscriptionQuery(
                new MoneyDebitNotifier(id),
                ResponseTypes.instanceOf(AccountEntity.class),
                ResponseTypes.instanceOf(AccountEntity.class));
        try {
            StepVerifier.create(result.updates())
                    .then(() -> eventHandler.on(new MoneyDebitedEvent(id, 30.0, "EUR")))
                    .assertNext(update -> assertThat(update.getAccountBalance()).isEqualTo(70.0))
                    .thenCancel()
                    .verify(Duration.ofSeconds(5));
        } finally {
            result.close();
        }
    }

    @Test
    @DisplayName("scatter-gather collects one answer per handler without changing the read model")
    void scatterGather_collectsIndependentAnswers() {
        String id = openAccount(40.0);

        List<Map<String, Object>> responses = client.get().uri("/bank-accounts/scatter/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(r -> (Object) ((Map<?, ?>) r.get("payload")).get("accountBalance"))
                .containsExactlyInAnyOrder(40.0, 50.0);
        Map<String, Object> stored = client.get().uri("/bank-accounts/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        assertThat(stored).containsEntry("accountBalance", 40.0);
    }

    private String openAccount(double balance) {
        String id = UUID.randomUUID().toString();
        eventHandler.on(new AccountCreatedEvent(id, balance, "EUR", Status.CREATED));
        return id;
    }

    private HttpStatus statusOf(String uri, Object... vars) {
        return client.get().uri(uri, vars)
                .exchange((req, res) -> HttpStatus.valueOf(res.getStatusCode().value()));
    }
}
