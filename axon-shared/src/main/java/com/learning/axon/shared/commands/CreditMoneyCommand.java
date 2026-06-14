package com.learning.axon.shared.commands;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** GoF: Command — carries intent to credit money to a bank account. */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CreditMoneyCommand extends BaseCommand<String> {

    private final double creditAmount;
    private final String currency;

    public CreditMoneyCommand(String id, double creditAmount, String currency) {
        super(id);
        this.creditAmount = creditAmount;
        this.currency = currency;
    }
}
