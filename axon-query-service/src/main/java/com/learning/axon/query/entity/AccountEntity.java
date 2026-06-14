package com.learning.axon.query.entity;

import com.learning.axon.shared.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA read-model entity — the query-side projection maintained by event handlers.
 * GoF: Singleton (managed as Spring bean via JPA repositories).
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account_details")
public class AccountEntity {

    @Id
    @Column(name = "account_id")
    private String id;

    @Column(name = "account_balance")
    private double accountBalance;

    @Column(name = "currency")
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
}
