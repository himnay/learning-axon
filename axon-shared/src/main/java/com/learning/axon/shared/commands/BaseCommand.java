package com.learning.axon.shared.commands;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * GoF: Command — base type for all Axon command messages.
 * The {@code @TargetAggregateIdentifier} tells Axon which aggregate instance to route the command to.
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseCommand<T> {

    @TargetAggregateIdentifier
    public final T id;
}
