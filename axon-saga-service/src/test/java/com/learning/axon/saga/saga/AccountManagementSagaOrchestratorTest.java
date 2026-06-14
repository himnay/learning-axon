package com.learning.axon.saga.saga;

import com.learning.axon.shared.commands.IssueDebitCardCommand;
import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.AccountActivatedEvent;
import com.learning.axon.shared.events.AccountUpdatedEvent;
import org.axonframework.test.matchers.Matchers;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.instanceOf;

/**
 * Unit tests for AccountManagementSagaOrchestrator using Axon's SagaTestFixture.
 * GoF: Template Method — fixture provides the given/when/then testing protocol.
 */
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
                .whenPublishingA(new AccountActivatedEvent("acc-1", 500.0, "USD", Status.ACTIVATED))
                .expectDispatchedCommandsMatching(
                        Matchers.listWithAnyOf(
                                Matchers.messageWithPayload(instanceOf(IssueDebitCardCommand.class))));
    }

    @Test
    @DisplayName("should end saga when AccountUpdatedEvent is received after account is activated")
    void onAccountUpdated_shouldEndSaga() {
        fixture.givenAPublished(new AccountActivatedEvent("acc-1", 500.0, "USD", Status.ACTIVATED))
                .whenPublishingA(new AccountUpdatedEvent("acc-1", Status.COMPLETED))
                .expectActiveSagas(0);
    }
}
