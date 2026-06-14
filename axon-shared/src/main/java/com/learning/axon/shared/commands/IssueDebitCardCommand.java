package com.learning.axon.shared.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/** GoF: Command — instructs the debit-card service to issue a new card. */
@Getter
@With
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class IssueDebitCardCommand {

    private String accountId;

    @TargetAggregateIdentifier
    private String debitCardId;
}
