package com.learning.axon.shared.commands;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** GoF: Command — carries intent to create a new bank account. */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CreateAccountCommand extends BaseCommand<String> {

    private final double accountBalance;
    private final String currency;

    public CreateAccountCommand(String id, double accountBalance, String currency) {
        super(id);
        this.accountBalance = accountBalance;
        this.currency = currency;
    }
}
