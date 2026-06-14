package com.learning.axon.command.config;

import com.learning.axon.command.aggregate.AccountAggregate;
import lombok.RequiredArgsConstructor;
import org.axonframework.common.caching.Cache;
import org.axonframework.common.caching.WeakReferenceCache;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotter;
import org.axonframework.spring.eventsourcing.SpringPrototypeAggregateFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * GoF: Factory Method — {@code accountAggregateRepository} creates AccountAggregate instances via a factory.
 * GoF: Strategy — pluggable snapshot trigger (event-count-based vs. time-based).
 */
@Configuration
@RequiredArgsConstructor
public class AxonSnapshotConfig {

    @Value("${axon.snapshot.threshold.limit:3}")
    private int snapshotThreshold;

    private final EventStore eventStore;

    @Bean
    public AsyncTaskExecutor snapshotExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("snapshot-");
        executor.initialize();
        return executor;
    }

    @Bean
    public SpringAggregateSnapshotter snapshotter(
            ParameterResolverFactory parameterResolverFactory,
            TransactionManager transactionManager) {
        return SpringAggregateSnapshotter.builder()
                .eventStore(eventStore)
                .parameterResolverFactory(parameterResolverFactory)
                .executor(snapshotExecutor())
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public AggregateFactory<AccountAggregate> accountAggregateFactory() {
        return new SpringPrototypeAggregateFactory<>("accountAggregate");
    }

    @Bean
    public EventSourcingRepository<AccountAggregate> accountAggregateRepository(
            Snapshotter snapshotter,
            ParameterResolverFactory parameterResolverFactory) {
        return EventSourcingRepository.builder(AccountAggregate.class)
                .aggregateFactory(accountAggregateFactory())
                .eventStore(eventStore)
                .parameterResolverFactory(parameterResolverFactory)
                .snapshotTriggerDefinition(new EventCountSnapshotTriggerDefinition(snapshotter, snapshotThreshold))
                .cache(eventCache())
                .build();
    }

    @Bean
    public Cache eventCache() {
        return new WeakReferenceCache();
    }

    @Bean
    public ListenerInvocationErrorHandler listenerInvocationErrorHandler() {
        return PropagatingErrorHandler.INSTANCE;
    }
}
