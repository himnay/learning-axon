package com.learning.axon.saga.config;

import org.axonframework.config.Configuration;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.springframework.context.annotation.Bean;

/**
 * GoF: Factory Method — provides the DeadlineManager bean.
 * SimpleDeadlineManager fires deadlines in-memory (no persistence across restarts).
 * For durable deadlines swap in {@code DbSchedulerDeadlineManager}, {@code JobRunrDeadlineManager}
 * or {@code QuartzDeadlineManager}. Axon's own {@code Configuration} is injected (the legacy
 * Spring {@code AxonConfiguration} bean was removed in Axon 4.6+).
 */
@org.springframework.context.annotation.Configuration
public class DeadlineManagerConfig {

    /** Defines the deadline manager bean. */
    @Bean
    public DeadlineManager deadlineManager(Configuration configuration) {
        return SimpleDeadlineManager.builder()
                .scopeAwareProvider(new ConfigurationScopeAwareProvider(configuration))
                .build();
    }
}
