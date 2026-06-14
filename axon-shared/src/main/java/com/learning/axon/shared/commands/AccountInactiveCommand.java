package com.learning.axon.shared.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/** GoF: Command — triggers a deadline-backed account inactivation. */
@Getter
@Builder
@ToString
public class AccountInactiveCommand {

    @TargetAggregateIdentifier
    private final String accountId;
}
