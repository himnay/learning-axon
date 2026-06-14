package com.learning.axon.saga.aggregate;

import com.learning.axon.shared.commands.AccountInactiveCommand;
import com.learning.axon.shared.commands.AccountUpdateCommand;
import com.learning.axon.shared.commands.CreateAccountCommand;
import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Duration;

/**
 * GoF: Command — handles CreateAccountCommand, AccountUpdateCommand, AccountInactiveCommand.
 * GoF: Observer — reacts to stored events via @EventSourcingHandler.
 * Demonstrates Axon Deadline Manager for time-based compensating transactions.
 */
@Slf4j
@Data
@NoArgsConstructor
@Aggregate
public class AccountAggregate {

    private static final String HOLD_DEADLINE = "holdAccountDeadline";

    @AggregateIdentifier
    private String accountId;

    private double accountBalance;
    private String currency;
    private Status status;

    @CommandHandler
    public AccountAggregate(CreateAccountCommand cmd) {
        AggregateLifecycle.apply(
                new AccountCreatedEvent(cmd.getId(), cmd.getAccountBalance(), cmd.getCurrency(), Status.CREATED));
    }

    @EventSourcingHandler
    protected void on(AccountCreatedEvent event) {
        this.accountId = event.getId();
        this.accountBalance = event.getAccountBalance();
        this.currency = event.getCurrency();
        this.status = Status.CREATED;
        AggregateLifecycle.apply(
                new AccountActivatedEvent(event.getId(), event.getAccountBalance(), event.getCurrency(), Status.ACTIVATED));
    }

    @EventSourcingHandler
    protected void on(AccountActivatedEvent event) {
        this.status = event.getStatus();
        log.info("Account [{}] activated — SAGA will begin", accountId);
    }

    @CommandHandler
    protected void on(AccountUpdateCommand cmd) {
        AggregateLifecycle.apply(new AccountUpdatedEvent(cmd.getAccountId(), cmd.getStatus()));
    }

    @EventSourcingHandler
    protected void on(AccountUpdatedEvent event) {
        this.accountId = event.getId();
        this.status = event.getStatus();
        log.info("Account [{}] updated to status [{}] — SAGA completed", accountId, status);
    }

    @CommandHandler
    public void on(AccountInactiveCommand cmd, DeadlineManager deadlineManager) {
        String deadlineId = deadlineManager.schedule(
                Duration.ofSeconds(60), HOLD_DEADLINE, cmd.getAccountId());
        AggregateLifecycle.apply(
                new AccountInactiveEvent(cmd.getAccountId(), Status.INACTIVE),
                MetaData.with(HOLD_DEADLINE, deadlineId));
    }

    @DeadlineHandler(deadlineName = HOLD_DEADLINE)
    public void onHoldDeadline(String accountId) {
        log.info("Deadline fired for account [{}] — apply any expiry logic here", accountId);
    }
}
