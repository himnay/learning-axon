package com.learning.axon.command.config;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.extensions.amqp.eventhandling.AMQPMessageConverter;
import org.axonframework.extensions.amqp.eventhandling.spring.SpringAMQPPublisher;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publishes every event applied on this service's event bus to the RabbitMQ topic exchange that
 * the query service's queue is bound to.
 *
 * <p>axon-amqp 4.12 would auto-configure this publisher, but on Spring Boot 4 it never does: its
 * {@code @AutoConfigureAfter} names Boot 3's {@code RabbitAutoConfiguration} package, so its
 * {@code @ConditionalOnBean(ConnectionFactory.class)} is evaluated before Boot defines the
 * connection factory and the publisher is silently skipped. Declaring it here restores it.
 */
@Configuration
@ConditionalOnProperty("axon.amqp.exchange")
public class AmqpPublisherConfig {

    /** Durable topic exchange; RabbitAdmin declares it on the first connection. */
    @Bean
    public TopicExchange eventsExchange(@Value("${axon.amqp.exchange}") String exchangeName) {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    /** Same wiring as axon-amqp's auto-configuration (transaction mode NONE, its default). */
    @Bean(initMethod = "start", destroyMethod = "shutDown")
    public SpringAMQPPublisher amqpBridge(EventBus eventBus,
                                          ConnectionFactory connectionFactory,
                                          AMQPMessageConverter amqpMessageConverter,
                                          @Value("${axon.amqp.exchange}") String exchangeName) {
        SpringAMQPPublisher publisher = new SpringAMQPPublisher(eventBus);
        publisher.setExchangeName(exchangeName);
        publisher.setConnectionFactory(connectionFactory);
        publisher.setMessageConverter(amqpMessageConverter);
        return publisher;
    }
}
