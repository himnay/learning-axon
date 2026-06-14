package com.learning.axon.query.config;

import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * GoF: Strategy — selects "subscribing" processing strategy so events from AMQP
 * are processed synchronously by the registered message source.
 */
@Configuration
public class AxonQueryConfig {

    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.usingSubscribingEventProcessors();
    }
}
