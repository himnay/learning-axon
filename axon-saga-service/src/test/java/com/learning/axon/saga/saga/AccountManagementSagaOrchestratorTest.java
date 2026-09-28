package com.learning.axon.saga.saga;

import com.learning.axon.shared.commands.AccountUpdateCommand;
import com.learning.axon.shared.commands.CancelIssuedChequeBookCommand;
import com.learning.axon.shared.commands.CancelIssuedDebitCardCommand;
import com.learning.axon.shared.commands.IssueChequeBookCommand;
import com.learning.axon.shared.commands.IssueDebitCardCommand;
import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.AccountActivatedEvent;
import com.learning.axon.shared.events.AccountUpdatedEvent;
import com.learning.axon.shared.events.ChequeBookIssuedEvent;
import com.learning.axon.shared.events.DebitCardIssuedEvent;
import org.axonframework.test.matchers.Matchers;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Unit tests for AccountManagementSagaOrchestrator using Axon's SagaTestFixture.
 * GoF: Template Method — fixture provides the given/when/then testing protocol.
 */
@DisplayName("AccountManagementSagaOrchestrator Unit Tests")
class AccountManagementSagaOrchestratorTest {

    private static final AccountActivatedEvent ACTIVATED =
            new AccountActivatedEvent("acc-1", 500.0, "USD", Status.ACTIVATED);

    private SagaTestFixture<AccountManagementSagaOrchestrator> fixture;

    /** Commands the saga dispatched; the saga mints the card and cheque-book ids itself. */
    private final List<Object> dispatched = new ArrayList<>();

    /** Set to a command type to make its dispatch fail, as a failing participant would. */
    private Class<?> failingCommand;

    @BeforeEach
    void setUp() {
        fixture = new SagaTestFixture<>(AccountManagementSagaOrchestrator.class);
        fixture.setCallbackBehavior((payload, metaData) -> {
            dispatched.add(payload);
            if (failingCommand != null && failingCommand.isInstance(payload)) {
                throw new IllegalStateException("participant rejected " + payload.getClass().getSimpleName());
            }
            return null;
        });
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

    @Test
    @DisplayName("should issue the cheque book once the debit card of this saga is issued")
    void onDebitCardIssued_shouldIssueChequeBook() {
        fixture.givenAPublished(ACTIVATED)
                .whenPublishingA(new DebitCardIssuedEvent("acc-1", issued(IssueDebitCardCommand.class).getDebitCardId()))
                .expectActiveSagas(1)
                .expectDispatchedCommandsMatching(Matchers.exactSequenceOf(
                        Matchers.messageWithPayload(instanceOf(IssueChequeBookCommand.class)),
                        Matchers.andNoMore()));
    }

    @Test
    @DisplayName("should complete the account once the cheque book of this saga is issued")
    void onChequeBookIssued_shouldCompleteAccount() {
        fixture.givenAPublished(ACTIVATED);
        String debitCardId = issued(IssueDebitCardCommand.class).getDebitCardId();
        fixture.givenAPublished(new DebitCardIssuedEvent("acc-1", debitCardId))
                .whenPublishingA(new ChequeBookIssuedEvent("acc-1", debitCardId,
                        issued(IssueChequeBookCommand.class).getChequeBookId()))
                .expectDispatchedCommandsMatching(Matchers.exactSequenceOf(
                        Matchers.messageWithPayload(instanceOf(AccountUpdateCommand.class)),
                        Matchers.andNoMore()));
        assertThat(issued(AccountUpdateCommand.class).getStatus()).isEqualTo(Status.COMPLETED);
    }

    @Test
    @DisplayName("should compensate the cheque book and the debit card when cheque-book issuance fails")
    void onChequeBookFailure_shouldCompensateBothSteps() {
        failingCommand = IssueChequeBookCommand.class;
        fixture.givenAPublished(ACTIVATED)
                .whenPublishingA(new DebitCardIssuedEvent("acc-1", issued(IssueDebitCardCommand.class).getDebitCardId()))
                .expectDispatchedCommandsMatching(Matchers.exactSequenceOf(
                        Matchers.messageWithPayload(instanceOf(IssueChequeBookCommand.class)),
                        Matchers.messageWithPayload(instanceOf(CancelIssuedChequeBookCommand.class)),
                        Matchers.messageWithPayload(instanceOf(CancelIssuedDebitCardCommand.class)),
                        Matchers.andNoMore()));
        String debitCardId = issued(IssueDebitCardCommand.class).getDebitCardId();
        assertThat(issued(CancelIssuedDebitCardCommand.class).getDebitCardId()).isEqualTo(debitCardId);
    }

    @Test
    @DisplayName("should cancel the debit card when its issuance fails")
    void onDebitCardFailure_shouldCancelDebitCard() {
        failingCommand = IssueDebitCardCommand.class;
        fixture.givenNoPriorActivity()
                .whenPublishingA(ACTIVATED)
                .expectDispatchedCommandsMatching(Matchers.exactSequenceOf(
                        Matchers.messageWithPayload(instanceOf(IssueDebitCardCommand.class)),
                        Matchers.messageWithPayload(instanceOf(CancelIssuedDebitCardCommand.class)),
                        Matchers.andNoMore()));
    }

    /** Last dispatched command of the given type. */
    private <T> T issued(Class<T> type) {
        return dispatched.stream().filter(type::isInstance).map(type::cast)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AssertionError("no " + type.getSimpleName() + " dispatched"));
    }
}
