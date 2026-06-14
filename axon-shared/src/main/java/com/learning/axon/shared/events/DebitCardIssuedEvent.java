package com.learning.axon.shared.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** GoF: Observer — published when a debit card is successfully issued by the debit-card service. */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardIssuedEvent {

    private String accountId;
    private String debitCardId;
}
