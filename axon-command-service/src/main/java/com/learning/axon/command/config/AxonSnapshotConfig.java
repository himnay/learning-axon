package com.learning.axon.command.config;

import org.axonframework.common.caching.Cache;
import org.axonframework.common.caching.WeakReferenceCache;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Snapshotting and caching for {@code AccountAggregate}.
 *
 * <p>Axon's Spring Boot auto-configuration builds the event-sourcing repository and the
 * {@link Snapshotter}; the aggregate opts in by name via
 * {@code @Aggregate(snapshotTriggerDefinition = "accountSnapshotTrigger", cache = "eventCache")}.
 *
 * <p>GoF: Strategy — the snapshot trigger is pluggable (event-count based here; a
 * time-based {@code AggregateLoadTimeSnapshotTriggerDefinition} is the alternative).
 */
@Configuration
public class AxonSnapshotConfig {

    /** Take a snapshot after this many events have been applied since the last one. */
    @Bean
    public SnapshotTriggerDefinition accountSnapshotTrigger(
            Snapshotter snapshotter,
            @Value("${axon.snapshot.threshold.limit:3}") int snapshotThreshold) {
        return new EventCountSnapshotTriggerDefinition(snapshotter, snapshotThreshold);
    }

    /** In-memory aggregate cache; weak references let the GC reclaim idle aggregates. */
    @Bean
    public Cache eventCache() {
        return new WeakReferenceCache();
    }

    /** Propagate event-handler failures instead of logging and continuing. */
    @Bean
    public ListenerInvocationErrorHandler listenerInvocationErrorHandler() {
        return PropagatingErrorHandler.INSTANCE;
    }
}
