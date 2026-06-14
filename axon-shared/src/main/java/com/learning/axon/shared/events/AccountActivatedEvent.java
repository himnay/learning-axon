package com.learning.axon.shared.events;

import com.learning.axon.shared.enums.Status;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** GoF: Observer — published when a bank account becomes active. */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AccountActivatedEvent extends BaseEvent<String> {

    private double accountBalance;
    private String currency;
    private Status status;

    public AccountActivatedEvent(String id, Status status) {
        super(id);
        this.status = status;
    }

    public AccountActivatedEvent(String id, double accountBalance, String currency, Status status) {
        super(id);
        this.accountBalance = accountBalance;
        this.currency = currency;
        this.status = status;
    }
}
