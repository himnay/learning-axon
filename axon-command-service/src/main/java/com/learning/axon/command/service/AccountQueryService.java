package com.learning.axon.command.service;

import java.util.List;

/**
 * GoF: Template Method — reads directly from the Axon event store for debugging / admin views.
 */
public interface AccountQueryService {

    List<Object> listEventsForAccount(String accountId);
}
