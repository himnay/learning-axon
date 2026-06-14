package com.learning.axon.chequebook.aggregate;

import com.learning.axon.chequebook.exception.CancelIssuedChequeBookException;
import com.learning.axon.shared.commands.CancelIssuedChequeBookCommand;
import com.learning.axon.shared.commands.IssueChequeBookCommand;
import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.ChequeBookIssuedEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

/**
 * GoF: Command — handles IssueChequeBookCommand and CancelIssuedChequeBookCommand.
 * GoF: Observer — updates state via @EventSourcingHandler.
 *
 * <p>Toggle {@code failure = true} to simulate a saga rollback scenario.
 */
@Slf4j
@Data
@NoArgsConstructor
@Aggregate
public class ChequeBookAggregate {

    @AggregateIdentifier
    private String chequeBookId;

    private String accountId;
    private String debitCardId;
    private Status status;

    /** Set to {@code true} to test saga compensating transactions. */
    private boolean failure = false;

    @CommandHandler
    public ChequeBookAggregate(IssueChequeBookCommand cmd) {
        log.info("Issuing cheque book for account [{}]", cmd.getAccountId());
        AggregateLifecycle.apply(
                new ChequeBookIssuedEvent(cmd.getAccountId(), cmd.getDebitCardId(), cmd.getChequeBookId()));
    }

    @CommandHandler
    public ChequeBookAggregate(CancelIssuedChequeBookCommand cmd) {
        log.info("Rolling back cheque book issuance for account [{}]", cmd.getAccountId());
        // Compensating action — fire a cancellation event if needed
    }

    @EventSourcingHandler
    protected void on(ChequeBookIssuedEvent event) {
        this.accountId = event.getAccountId();
        this.debitCardId = event.getDebitCardId();
        this.chequeBookId = event.getChequeBookId();
        this.status = Status.CHEQUE_BOOK_ISSUED;

        if (failure) {
            throw new CancelIssuedChequeBookException(
                    "Simulated cheque book failure — triggering saga rollback");
        }

        log.info("Cheque book [{}] issued for account [{}]", chequeBookId, accountId);
    }
}
