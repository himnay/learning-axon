package com.learning.axon.chequebook.aggregate;

import com.learning.axon.shared.commands.IssueChequeBookCommand;
import com.learning.axon.shared.events.ChequeBookIssuedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ChequeBookAggregate Unit Tests")
class ChequeBookAggregateTest {

    private FixtureConfiguration<ChequeBookAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(ChequeBookAggregate.class);
    }

    @Test
    @DisplayName("should publish ChequeBookIssuedEvent when IssueChequeBookCommand is received")
    void issueChequeBook_shouldPublishEvent() {
        fixture.givenNoPriorActivity()
                .when(IssueChequeBookCommand.builder()
                        .accountId("acc-1")
                        .debitCardId("dc-1")
                        .chequeBookId("cb-1")
                        .build())
                .expectSuccessfulHandlerExecution()
                .expectEvents(new ChequeBookIssuedEvent("acc-1", "dc-1", "cb-1"));
    }
}
