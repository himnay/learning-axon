package com.learning.axon.shared.commands;

import com.learning.axon.shared.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/** GoF: Command — compensating command for an AccountUpdateCommand rollback. */
@Getter
@ToString
@AllArgsConstructor
public class CancelAccountUpdateCommand {

    @TargetAggregateIdentifier
    private final String accountId;
    private final Status status;
}
