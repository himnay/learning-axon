package com.learning.axon.debitcard.aggregate;

import com.learning.axon.shared.commands.CancelIssuedDebitCardCommand;
import com.learning.axon.shared.commands.IssueDebitCardCommand;
import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.DebitCardIssuedEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

/**
 * GoF: Command — handles IssueDebitCardCommand and CancelIssuedDebitCardCommand.
 * GoF: Observer — updates state via @EventSourcingHandler.
 */
@Slf4j
@Data
@NoArgsConstructor
@Aggregate
public class DebitCardAggregate {

    @AggregateIdentifier
    private String debitCardId;

    private String accountId;
    private Status status;

    @CommandHandler
    public DebitCardAggregate(IssueDebitCardCommand cmd) {
        log.info("Issuing debit card for account [{}]", cmd.getAccountId());
        AggregateLifecycle.apply(new DebitCardIssuedEvent(cmd.getAccountId(), cmd.getDebitCardId()));
    }

    @CommandHandler
    public DebitCardAggregate(CancelIssuedDebitCardCommand cmd) {
        log.info("Rolling back debit card issuance for account [{}]", cmd.getAccountId());
        // Compensating action — fire a cancellation event if needed
    }

    @EventSourcingHandler
    protected void on(DebitCardIssuedEvent event) {
        this.debitCardId = event.getDebitCardId();
        this.accountId = event.getAccountId();
        this.status = Status.DEBIT_CARD_ISSUED;
        log.info("Debit card [{}] issued for account [{}]", debitCardId, accountId);
    }
}
