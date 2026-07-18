package com.learning.axon.shared.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** DTO for the debit-money REST endpoint. */
public record MoneyDebitRequest(
        @Positive double debitAmount,
        @NotBlank String currency
) {}
