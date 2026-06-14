package com.learning.axon.query.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.extensions.amqp.eventhandling.AMQPMessageConverter;
import org.axonframework.extensions.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GoF: Observer — wires Axon's AMQP message source to the RabbitMQ queue.
 * Events published by the command service are received here and dispatched to {@code AccountEventHandler}.
 */
@Slf4j
@Configuration
public class AmqpEventListener {

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
