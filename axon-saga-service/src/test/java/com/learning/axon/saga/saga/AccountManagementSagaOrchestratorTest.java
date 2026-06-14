package com.learning.axon.saga.saga;

import com.learning.axon.shared.commands.IssueDebitCardCommand;
import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.AccountActivatedEvent;
import com.learning.axon.shared.events.AccountUpdatedEvent;
import com.learning.axon.shared.events.ChequeBookIssuedEvent;
import com.learning.axon.shared.events.DebitCardIssuedEvent;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AccountManagementSagaOrchestrator Unit Tests")
class AccountManagementSagaOrchestratorTest {

    private SagaTestFixture<AccountManagementSagaOrchestrator> fixture;

    @BeforeEach
    void setUp() {
        fixture = new SagaTestFixture<>(AccountManagementSagaOrchestrator.class);
    }

    @Test
    @DisplayName("should dispatch IssueDebitCardCommand when AccountActivatedEvent is received")
    void onAccountActivated_shouldIssueDebitCard() {
        fixture.givenNoPriorActivity()
                .whenAggregateLikes(new AccountActivatedEvent("acc-1", 500.0, "USD", Status.ACTIVATED))
                .expectDispatchedCommandsMatching(
                        cmd -> cmd.stream().anyMatch(m -> m.getPayload() instanceof IssueDebitCardCommand));
    }

    @Test
    @DisplayName("should end saga when AccountUpdatedEvent is received")
    void onAccountUpdated_shouldEndSaga() {
        String debitCardId = "dc-1";
        String chequeBookId = "cb-1";

        fixture.givenAPublished(new AccountActivatedEvent("acc-1", 500.0, "USD", Status.ACTIVATED))
                .andThenAPublished(new DebitCardIssuedEvent("acc-1", debitCardId))
                .andThenAPublished(new ChequeBookIssuedEvent("acc-1", debitCardId, chequeBookId))
                .whenAggregateLikes(new AccountUpdatedEvent("acc-1", Status.COMPLETED))
                .expectActiveSagas(0);
    }
}
