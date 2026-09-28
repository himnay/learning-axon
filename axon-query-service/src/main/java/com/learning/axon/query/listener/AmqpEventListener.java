package com.learning.axon.query.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.extensions.amqp.eventhandling.AMQPMessageConverter;
import org.axonframework.extensions.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GoF: Observer — wires Axon's AMQP message source to the RabbitMQ queue.
 * Events published by the command service are received here and dispatched to {@code AccountEventHandler}.
 *
 * <p>The exchange, queue and binding are declared here (RabbitAdmin creates them on the first
 * connection), so the flow works against a fresh broker. The command service publishes with the
 * event's package as routing key; {@code #} binds every key.
 */
@Slf4j
@Configuration
public class AmqpEventListener {

    /** Topic exchange the command service publishes to (declared on both sides; declaring is idempotent). */
    @Bean
    public TopicExchange eventsExchange(@Value("${axon.amqp.exchange:axon.event.sourcing.topic}") String exchangeName) {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    /** Durable queue this service consumes, so events published while it is down wait for it. */
    @Bean
    public Queue eventsQueue(@Value("${axon.amqp.queue:axon.event.sourcing.topic.queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    /** Routes every event on the exchange into the queue. */
    @Bean
    public Binding eventsBinding(Queue eventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(eventsQueue).to(eventsExchange).with("#");
    }

    /** Defines the account message source bean. */
    @Bean
    public SpringAMQPMessageSource accountMessageSource(AMQPMessageConverter messageConverter) {
        return new SpringAMQPMessageSource(messageConverter) {
            @Override
            @RabbitListener(queues = "${axon.amqp.queue:axon.event.sourcing.topic.queue}")
            public void onMessage(Message message, Channel channel) {
                log.info("AMQP event received: [{}]", message);
                super.onMessage(message, channel);
            }
        };
    }
}
