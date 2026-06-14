package com.learning.axon.command.controller;

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
@RequestMapping("/bank-accounts")
@RequiredArgsConstructor
public class AccountCommandController {

    private final AccountCommandService accountCommandService;
    private final EventProcessingConfiguration eventProcessingConfiguration;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createAccount(@Valid @RequestBody AccountCreateRequest request) {
        return accountCommandService.createAccount(request);
    }

    @PutMapping("/credits/{accountId}")
    public CompletableFuture<String> creditMoney(
            @PathVariable String accountId,
            @Valid @RequestBody MoneyCreditRequest request) {
        return accountCommandService.creditMoneyToAccount(accountId, request);
    }

    @PutMapping("/debits/{accountId}")
    public String debitMoney(
            @PathVariable String accountId,
            @Valid @RequestBody MoneyDebitRequest request) {
        return accountCommandService.debitMoneyFromAccount(accountId, request);
    }

    /**
     * Trigger event replay for the Tracking Event Processor.
     * GoF: Strategy — swaps processing strategy at runtime.
     */
    @PostMapping("/replay")
    public ResponseEntity<String> replay() {
        eventProcessingConfiguration
                .eventProcessorByProcessingGroup("account_tep_group", TrackingEventProcessor.class)
                .ifPresent(tep -> {
                    tep.shutDown();
                    tep.resetTokens();
                    tep.start();
                });
        return ResponseEntity.ok("Replay triggered");
    }

    @GetMapping("/status")
    public Map<Integer, EventTrackerStatus> status() {
        return eventProcessingConfiguration
                .eventProcessorByProcessingGroup("account_tep_group", TrackingEventProcessor.class)
                .map(TrackingEventProcessor::processingStatus)
                .orElseThrow(() -> new IllegalStateException("TrackingEventProcessor not found"));
    }
}
