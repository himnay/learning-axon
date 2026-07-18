package com.learning.axon.shared.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/** GoF: Command — compensating command to cancel a previously issued cheque book (saga rollback). */
@With
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CancelIssuedChequeBookCommand {

    private String accountId;
    private String debitCardId;

    @TargetAggregateIdentifier
    private String chequeBookId;
}
