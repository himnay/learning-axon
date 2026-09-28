package com.learning.axon.command.integration;

import com.learning.axon.shared.events.AccountCreatedEvent;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.extensions.amqp.eventhandling.AMQPMessageConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The command service must publish every applied event to the RabbitMQ topic exchange: create an
 * account over HTTP and read the published message back from a queue bound to that exchange.
 * Guards against axon-amqp's auto-configured publisher being skipped on Spring Boot 4.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.autoconfigure.exclude=",                 // the test profile excludes RabbitMQ; bring it back
        "axon.amqp.exchange=axon.event.sourcing.topic"
})
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Command Service AMQP publishing")
class AmqpPublishingIntegrationTest {

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
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AMQPMessageConverter messageConverter;

    @Test
    @DisplayName("creating an account publishes AccountCreatedEvent to the topic exchange")
    void createAccount_publishesEventToExchange() {
        Queue probe = amqpAdmin.declareQueue();          // server-named, exclusive, auto-delete
        amqpAdmin.declareBinding(BindingBuilder.bind(probe)
                .to(new TopicExchange("axon.event.sourcing.topic")).with("#"));

        String accountId = RestClient.create("http://localhost:" + port).post().uri("/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("startingBalance", 50.0, "currency", "EUR"))
                .retrieve()
                .body(String.class);

        Message message = rabbitTemplate.receive(probe.getName(), 10_000);
        assertThat(message).as("event published to RabbitMQ").isNotNull();
        Optional<EventMessage<?>> event = messageConverter.readAMQPMessage(
                message.getBody(), message.getMessageProperties().getHeaders());
        assertThat(event).get()
                .satisfies(e -> assertThat(e.getPayload()).isInstanceOfSatisfying(AccountCreatedEvent.class,
                        created -> assertThat(created.getId()).isEqualTo(accountId)));
    }
}
