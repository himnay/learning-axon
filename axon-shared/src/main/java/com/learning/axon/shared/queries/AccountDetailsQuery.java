package com.learning.axon.shared.queries;

/** GoF: Command (query variant) — carries pagination params for Axon subscription queries. */
public record AccountDetailsQuery(String id, int offset, int limit) {}
