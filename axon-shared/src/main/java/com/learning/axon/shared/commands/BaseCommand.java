package com.learning.axon.shared.commands;

import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * GoF: Command — base type for all Axon command messages.
 * The {@code @TargetAggregateIdentifier} tells Axon which aggregate instance to route the command to.
 */
@Data
public abstract class BaseCommand<T> {

    @TargetAggregateIdentifier
    public final T id;

    protected BaseCommand(T id) {
        this.id = id;
    }
}
