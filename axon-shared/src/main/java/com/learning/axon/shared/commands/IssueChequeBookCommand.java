package com.learning.axon.shared.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/** GoF: Command — instructs the cheque-book service to issue a new cheque book. */
@Getter
@With
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class IssueChequeBookCommand {

    private String accountId;
    private String debitCardId;

    @TargetAggregateIdentifier
    private String chequeBookId;
}
