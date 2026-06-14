package com.learning.axon.saga.saga;

import com.learning.axon.shared.commands.*;
import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.AccountActivatedEvent;
import com.learning.axon.shared.events.AccountUpdatedEvent;
import com.learning.axon.shared.events.ChequeBookIssuedEvent;
import com.learning.axon.shared.events.DebitCardIssuedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * GoF: Chain of Responsibility — the Saga coordinates a chain of compensating steps.
 * GoF: Observer — reacts to events published by various aggregates.
 *
 * <p>Account-opening flow:
 * <ol>
 *   <li>{@code AccountActivatedEvent} → Issue Debit Card</li>
 *   <li>{@code DebitCardIssuedEvent} → Issue Cheque Book</li>
 *   <li>{@code ChequeBookIssuedEvent} → Update Account to COMPLETED</li>
 *   <li>{@code AccountUpdatedEvent} → End Saga</li>
 * </ol>
 * Any step failure triggers compensating commands (rollback).
 */
@Slf4j
@Saga
public class AccountManagementSagaOrchestrator {

    @Autowired
    private transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    public void handle(AccountActivatedEvent event) {
        log.info("SAGA started — AccountActivatedEvent [{}]", event);
        String debitCardId = UUID.randomUUID().toString();
        SagaLifecycle.associateWith("debitCardId", debitCardId);

        IssueDebitCardCommand cmd = IssueDebitCardCommand.builder()
                .accountId(event.getId())
                .debitCardId(debitCardId)
                .build();

        commandGateway.send(cmd, (message, result) -> {
            if (result.isExceptional()) {
                log.error("Debit card issuance failed — rolling back SAGA");
                commandGateway.send(CancelIssuedDebitCardCommand.builder()
                        .accountId(cmd.getAccountId())
                        .debitCardId(cmd.getDebitCardId())
                        .build());
            }
        });
    }

    @SagaEventHandler(associationProperty = "debitCardId")
    public void handle(DebitCardIssuedEvent event) {
        log.info("SAGA step 2 — DebitCardIssuedEvent [{}]", event);
        String chequeBookId = UUID.randomUUID().toString();
        SagaLifecycle.associateWith("chequeBookId", chequeBookId);

        IssueChequeBookCommand cmd = IssueChequeBookCommand.builder()
                .accountId(event.getAccountId())
                .debitCardId(event.getDebitCardId())
                .chequeBookId(chequeBookId)
                .build();

        commandGateway.send(cmd, (message, result) -> {
            if (result.isExceptional()) {
                log.error("Cheque book issuance failed — rolling back SAGA");
                commandGateway.send(CancelIssuedChequeBookCommand.builder()
                        .accountId(cmd.getAccountId())
                        .debitCardId(cmd.getDebitCardId())
                        .chequeBookId(cmd.getChequeBookId())
                        .build());
                commandGateway.send(CancelIssuedDebitCardCommand.builder()
                        .accountId(cmd.getAccountId())
                        .debitCardId(cmd.getDebitCardId())
                        .build());
            }
        });
    }

    @SagaEventHandler(associationProperty = "chequeBookId")
    public void handle(ChequeBookIssuedEvent event) {
        log.info("SAGA step 3 — ChequeBookIssuedEvent [{}], completing account", event);
        SagaLifecycle.associateWith("accountId", event.getAccountId());

        AccountUpdateCommand cmd = AccountUpdateCommand.builder()
                .accountId(event.getAccountId())
                .status(Status.COMPLETED)
                .build();

        commandGateway.send(cmd, (message, result) -> {
            if (result.isExceptional()) {
                log.error("Account update failed — rolling back");
                commandGateway.send(new CancelAccountUpdateCommand(event.getAccountId(), Status.COMPLETED));
            }
        });
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(AccountUpdatedEvent event) {
        log.info("SAGA completed — AccountUpdatedEvent [{}]", event);
        SagaLifecycle.end();
    }
}
