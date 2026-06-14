package com.learning.axon.shared.commands;

import com.learning.axon.shared.enums.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/** GoF: Command — instructs the saga account aggregate to mark an account as fully set up. */
@Getter
@Builder
@ToString
public class AccountUpdateCommand {

    @TargetAggregateIdentifier
    private final String accountId;
    private final Status status;
}
