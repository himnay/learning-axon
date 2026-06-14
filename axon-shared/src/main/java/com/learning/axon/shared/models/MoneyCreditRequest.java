package com.learning.axon.shared.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** DTO for the credit-money REST endpoint. */
public record MoneyCreditRequest(
        @Positive double creditAmount,
        @NotBlank String currency
) {}
