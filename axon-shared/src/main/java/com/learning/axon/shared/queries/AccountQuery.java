package com.learning.axon.shared.queries;

/** GoF: Command (query variant) — carries the account number for point-to-point Axon query. */
public record AccountQuery(String accountNumber) {}
