package com.learning.axon.query.integration;

import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.AccountCreatedEvent;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.extensions.amqp.eventhandling.AMQPMessage;
import org.axonframework.extensions.amqp.eventhandling.AMQPMessageConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * The query service must consume events from a fresh RabbitMQ broker: it declares the exchange,
 * queue and binding itself, and an event published the way the command service's
 * {@code SpringAMQPPublisher} publishes it must end up in the read model.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.autoconfigure.exclude=",                 // the test profile excludes RabbitMQ; bring it back
        "axon.amqp.exchange=axon.event.sourcing.topic",
        "axon.amqp.queue=axon.event.sourcing.topic.queue",
        "axon.eventhandling.processors.amqpEvents.source=accountMessageSource",
        "axon.eventhandling.processors.amqpEvents.mode=subscribing"
})
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Query Service AMQP consumption")
class AmqpConsumingIntegrationTest {

    @Container
    static final RabbitMQContainer RABBIT =
            new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.3-management-alpine"));

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AMQPMessageConverter messageConverter;

    @Test
    @DisplayName("AccountCreatedEvent published to the exchange is projected into account_details")
    void publishedEvent_isProjected() {
        String accountId = UUID.randomUUID().toString();
        AMQPMessage amqp = messageConverter.createAMQPMessage(GenericEventMessage.asEventMessage(
                new AccountCreatedEvent(accountId, 75.0, "EUR", Status.CREATED)));
        rabbitTemplate.execute(channel -> {
            channel.basicPublish("axon.event.sourcing.topic", amqp.getRoutingKey(),
                    amqp.isMandatory(), amqp.isImmediate(), amqp.getProperties(), amqp.getBody());
            return null;
        });

        RestClient client = RestClient.create("http://localhost:" + port);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            HttpStatus status = client.get().uri("/bank-accounts/{id}", accountId)
                    .exchange((req, res) -> HttpStatus.valueOf(res.getStatusCode().value()));
            assertThat(status).isEqualTo(HttpStatus.OK);
        });
        Map<String, Object> account = client.get().uri("/bank-accounts/{id}", accountId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        assertThat(account).containsEntry("accountBalance", 75.0).containsEntry("status", "CREATED");
    }
}
