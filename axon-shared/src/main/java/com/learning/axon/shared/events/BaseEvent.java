package com.learning.axon.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GoF: Observer — base class for all domain events published to Axon's event bus.
 * Concrete events extend this and carry the aggregate identifier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent<T> {

    protected T id;
}
