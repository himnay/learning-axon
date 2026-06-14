package com.learning.axon.shared.events;

import com.learning.axon.shared.enums.Status;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** GoF: Observer — published when an account balance goes negative (account on hold). */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AccountHeldEvent extends BaseEvent<String> {

    private Status status;

    public AccountHeldEvent(String id, Status status) {
        super(id);
        this.status = status;
    }
}
