package com.learning.axon.command.controller;

import com.learning.axon.command.projection.AccountActivityProjection;
import com.learning.axon.command.service.AccountCommandService;
import com.learning.axon.shared.models.AccountCreateRequest;
import com.learning.axon.shared.models.MoneyCreditRequest;
import com.learning.axon.shared.models.MoneyDebitRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.EventTrackerStatus;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * GoF: Chain of Responsibility — HTTP request → Controller → Service → CommandGateway → Aggregate.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bank-accounts")
public class AccountCommandController {

    private final AccountCommandService accountCommandService;
    private final EventProcessingConfiguration eventProcessingConfiguration;
    private final AccountActivityProjection accountActivityProjection;

    /** Creates account. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createAccount(@Valid @RequestBody AccountCreateRequest request) {
        return accountCommandService.createAccount(request);
    }

    /** Returns the credit money. */
    @PutMapping("/credits/{accountId}")
    public CompletableFuture<String> creditMoney(
            @PathVariable String accountId,
            @Valid @RequestBody MoneyCreditRequest request) {
        return accountCommandService.creditMoneyToAccount(accountId, request);
    }

    /** Returns the debit money. */
    @PutMapping("/debits/{accountId}")
    public String debitMoney(
            @PathVariable String accountId,
            @Valid @RequestBody MoneyDebitRequest request) {
        return accountCommandService.debitMoneyFromAccount(accountId, request);
    }

    /**
     * Replays the event store into {@link AccountActivityProjection}: stop its Tracking Event
     * Processor, reset the token to the start of the stream, and start it again.
     * GoF: Strategy — swaps processing strategy at runtime.
     */
    @PostMapping("/replay")
    public ResponseEntity<String> replay() {
        TrackingEventProcessor tep = trackingProcessor();
        tep.shutDown();
        tep.resetTokens();
        tep.start();
        return ResponseEntity.ok("Replay triggered");
    }

    /** Per-segment status of the tracking processor (position, caught up, replaying). */
    @GetMapping("/status")
    public Map<Integer, EventTrackerStatus> status() {
        return trackingProcessor().processingStatus();
    }

    /** Number of events the replayable projection has seen for the account. */
    @GetMapping("/{accountId}/activity")
    public Map<String, Object> activity(@PathVariable String accountId) {
        return Map.of("accountId", accountId, "events", accountActivityProjection.eventCount(accountId));
    }

    private TrackingEventProcessor trackingProcessor() {
        return eventProcessingConfiguration
                .eventProcessorByProcessingGroup(AccountActivityProjection.PROCESSING_GROUP, TrackingEventProcessor.class)
                .orElseThrow(() -> new IllegalStateException(
                        "No TrackingEventProcessor for " + AccountActivityProjection.PROCESSING_GROUP));
    }
}
