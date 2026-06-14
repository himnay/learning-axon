package com.learning.axon.shared.events;

import com.learning.axon.shared.enums.Status;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** GoF: Observer — published when a deadline fires and the account becomes INACTIVE. */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AccountInactiveEvent extends BaseEvent<String> {

    private Status status;

    public AccountInactiveEvent(String id, Status status) {
        super(id);
        this.status = status;
    }
}
