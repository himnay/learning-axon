package com.learning.axon.debitcard.aggregate;

import com.learning.axon.shared.commands.IssueDebitCardCommand;
import com.learning.axon.shared.events.DebitCardIssuedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DebitCardAggregate Unit Tests")
class DebitCardAggregateTest {

    private FixtureConfiguration<DebitCardAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(DebitCardAggregate.class);
    }

    @Test
    @DisplayName("should publish DebitCardIssuedEvent when IssueDebitCardCommand is received")
    void issueDebitCard_shouldPublishEvent() {
        fixture.givenNoPriorActivity()
                .when(IssueDebitCardCommand.builder()
                        .accountId("acc-1")
                        .debitCardId("dc-1")
                        .build())
                .expectSuccessfulHandlerExecution()
                .expectEvents(new DebitCardIssuedEvent("acc-1", "dc-1"));
    }
}
