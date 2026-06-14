package com.learning.axon.shared.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** GoF: Builder (implicit via Jackson) — DTO for account creation REST endpoint. */
public record AccountCreateRequest(
        @Positive double startingBalance,
        @NotBlank String currency
) {}
