package com.learning.axon.query.config;

import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * GoF: Strategy — selects "subscribing" processing strategy so events from AMQP
 * are processed synchronously by the registered message source.
 *
 * <p>Method @Autowired is intentional here: Spring calls this configurer callback
 * during context initialisation before any event processors start. This is the
 * idiomatic Axon pattern for overriding the default tracking processor.
 */
@Configuration
public class AxonQueryConfig {

    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.usingSubscribingEventProcessors();
    }
}
