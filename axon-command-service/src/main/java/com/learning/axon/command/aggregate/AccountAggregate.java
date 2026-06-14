package com.learning.axon.command.aggregate;

import com.learning.axon.shared.commands.CreateAccountCommand;
import com.learning.axon.shared.commands.CreditMoneyCommand;
import com.learning.axon.shared.commands.DebitMoneyCommand;
import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

/**
 * GoF: Command — receives commands and applies domain events.
 * GoF: Observer — via EventSourcingHandler methods that react to stored events.
 * GoF: Factory — {@code accountAggregateRepository} bean (AxonSnapshotConfig) creates instances.
 *
 * <p>Snapshot threshold configured in {@code AxonSnapshotConfig} controls when a snapshot is taken.
 */
@Slf4j
@Data
@NoArgsConstructor
@ProcessingGroup("account_tep_group")
@Aggregate(repository = "accountAggregateRepository")
public class AccountAggregate {

    @AggregateIdentifier
    private String id;

    private double accountBalance;
    private String currency;
    private Status status;

    // ── Command handlers ──────────────────────────────────────────────────────

    @CommandHandler
    public AccountAggregate(CreateAccountCommand cmd) {
        AggregateLifecycle.apply(
                new AccountCreatedEvent(cmd.getId(), cmd.getAccountBalance(), cmd.getCurrency(), Status.CREATED));
    }

    @CommandHandler
    protected void on(CreditMoneyCommand cmd) {
        AggregateLifecycle.apply(
                new MoneyCreditedEvent(cmd.getId(), cmd.getCreditAmount(), cmd.getCurrency()));
    }

    @CommandHandler
    protected void on(DebitMoneyCommand cmd) {
        AggregateLifecycle.apply(
                new MoneyDebitedEvent(cmd.getId(), cmd.getDebitAmount(), cmd.getCurrency()));
    }

    // ── Event sourcing handlers ───────────────────────────────────────────────

    @EventSourcingHandler
    protected void on(AccountCreatedEvent event) {
        this.id = event.getId();
        this.accountBalance = event.getAccountBalance();
        this.currency = event.getCurrency();
        this.status = Status.CREATED;

        if (accountBalance > 100) {
            AggregateLifecycle.apply(new AccountActivatedEvent(this.id, currency, Status.ACTIVATED));
        }
    }

    @EventSourcingHandler
    protected void on(AccountActivatedEvent event) {
        this.status = event.getStatus();
    }

    @EventSourcingHandler
    protected void on(MoneyCreditedEvent event) {
        if (this.accountBalance < 0 && (this.accountBalance + event.getCreditAmount()) >= 0) {
            AggregateLifecycle.apply(new AccountActivatedEvent(this.id, Status.ACTIVATED));
        }
        this.accountBalance += event.getCreditAmount();
    }

    @EventSourcingHandler
    protected void on(MoneyDebitedEvent event) {
        if (this.accountBalance >= 0 && (this.accountBalance - event.getDebitAmount()) < 0) {
            AggregateLifecycle.apply(new AccountHeldEvent(this.id, Status.HOLD));
        }
        this.accountBalance -= event.getDebitAmount();
    }

    @EventSourcingHandler
    protected void on(AccountHeldEvent event) {
        this.status = event.getStatus();
    }

    @ResetHandler
    public void onReset() {
        log.info("Pre-reset: clearing projection state before replay starts");
    }
}
