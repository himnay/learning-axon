package com.learning.axon.shared.commands;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** GoF: Command — carries intent to debit money from a bank account. */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DebitMoneyCommand extends BaseCommand<String> {

    private final double debitAmount;
    private final String currency;

    public DebitMoneyCommand(String id, double debitAmount, String currency) {
        super(id);
        this.debitAmount = debitAmount;
        this.currency = currency;
    }
}
