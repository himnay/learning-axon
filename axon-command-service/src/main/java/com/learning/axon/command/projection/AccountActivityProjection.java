package com.learning.axon.command.projection;

import com.learning.axon.shared.events.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command-side projection run by a Tracking Event Processor: counts the stored events per account.
 *
 * <p>It is what {@code POST /bank-accounts/replay} rebuilds. A replay resets the processor's
 * tracking token to the start of the event store, Axon calls {@link #onReset()} first, and then
 * every stored event is delivered to {@link #on(BaseEvent)} again. Without the reset handler the
 * counts would double on every replay.
 *
 * <p>Aggregates are not event processors: their {@code @EventSourcingHandler}s only run while Axon
 * sources an aggregate instance, so a replay needs a projection like this one.
 */
@Slf4j
@Component
@ProcessingGroup(AccountActivityProjection.PROCESSING_GROUP)
public class AccountActivityProjection {

    /** Name of the tracking processor that owns this projection (and of its token). */
    public static final String PROCESSING_GROUP = "account_tep_group";

    private final Map<String, Long> eventCounts = new ConcurrentHashMap<>();

    /** Counts every account event, whatever its type. */
    @EventHandler
    public void on(BaseEvent<String> event) {
        eventCounts.merge(event.getId(), 1L, Long::sum);
    }

    /** Called by Axon before a replay starts: drop the state the replay is about to rebuild. */
    @ResetHandler
    public void onReset() {
        log.info("Resetting {} before replay", PROCESSING_GROUP);
        eventCounts.clear();
    }

    /** Number of events this projection has seen for the account. */
    public long eventCount(String accountId) {
        return eventCounts.getOrDefault(accountId, 0L);
    }
}
