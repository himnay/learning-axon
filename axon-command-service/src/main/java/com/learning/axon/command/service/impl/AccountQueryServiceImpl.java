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

    /**
     * Reads the account's full event history. {@code readEvents(id)} would start from the latest
     * snapshot (taken every 3 events here) and return the aggregate's state in place of the events
     * before it; reading from sequence number 0 goes to the stored events only.
     */
    @Override
    public List<Object> listEventsForAccount(String accountId) {
        return eventStore.readEvents(accountId, 0)
                .asStream()
                .map(msg -> msg.getPayload())
                .collect(Collectors.toList());
    }
}
