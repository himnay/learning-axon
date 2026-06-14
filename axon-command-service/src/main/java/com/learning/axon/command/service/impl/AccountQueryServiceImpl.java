package com.learning.axon.command.service.impl;

import com.learning.axon.command.service.AccountQueryService;
import lombok.RequiredArgsConstructor;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GoF: Template Method — concrete impl that reads directly from the Axon event store.
 */
@Service
@RequiredArgsConstructor
public class AccountQueryServiceImpl implements AccountQueryService {

    private final EventStore eventStore;

    @Override
    public List<Object> listEventsForAccount(String accountId) {
        return eventStore.readEvents(accountId)
                .asStream()
                .map(msg -> msg.getPayload())
                .collect(Collectors.toList());
    }
}
