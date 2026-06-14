package com.learning.axon.shared.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** GoF: Observer — published when a cheque book is successfully issued by the cheque-book service. */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChequeBookIssuedEvent {

    private String accountId;
    private String debitCardId;
    private String chequeBookId;
}
