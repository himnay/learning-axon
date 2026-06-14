package com.learning.axon.saga.config;

import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GoF: Factory Method — provides the DeadlineManager bean.
 * SimpleDeadlineManager fires deadlines in-memory (no persistence across restarts).
 * Swap for {@code JpaDeadlineManager} in production if durability is required.
 */
@Configuration
public class DeadlineManagerConfig {

    @Bean
    public DeadlineManager deadlineManager(AxonConfiguration configuration) {
        return SimpleDeadlineManager.builder()
                .scopeAwareProvider(new ConfigurationScopeAwareProvider(configuration))
                .build();
    }
}
