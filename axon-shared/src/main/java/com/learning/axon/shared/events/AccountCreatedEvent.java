package com.learning.axon.shared.events;

import com.learning.axon.shared.enums.Status;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** GoF: Observer — published when a new bank account is created. */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AccountCreatedEvent extends BaseEvent<String> {

    private double accountBalance;
    private String currency;
    private Status status;

    public AccountCreatedEvent(String id, double accountBalance, String currency, Status status) {
        super(id);
        this.accountBalance = accountBalance;
        this.currency = currency;
        this.status = status;
    }
}
