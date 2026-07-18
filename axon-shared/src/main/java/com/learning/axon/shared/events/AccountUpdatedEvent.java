package com.learning.axon.shared.events;

import com.learning.axon.shared.enums.Status;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** GoF: Observer — published when the saga completes and the account is marked as COMPLETED. */
@Getter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AccountUpdatedEvent extends BaseEvent<String> {

    private Status status;

    public AccountUpdatedEvent(String id, Status status) {
        super(id);
        this.status = status;
    }
}
