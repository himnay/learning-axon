package com.learning.axon.command.aggregate;

import com.learning.axon.shared.commands.CreateAccountCommand;
import com.learning.axon.shared.commands.CreditMoneyCommand;
import com.learning.axon.shared.commands.DebitMoneyCommand;
import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.*;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AccountAggregate using Axon's AggregateTestFixture.
 * GoF: Template Method — fixture provides the testing protocol.
 */
@DisplayName("AccountAggregate Unit Tests")
class AccountAggregateTest {

    private FixtureConfiguration<AccountAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(AccountAggregate.class);
    }

    @Test
    @DisplayName("should publish AccountCreatedEvent and AccountActivatedEvent when balance > 100")
    void createAccount_withBalanceOver100_shouldActivate() {
        fixture.givenNoPriorActivity()
                .when(new CreateAccountCommand("acc-1", 500.0, "USD"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        new AccountCreatedEvent("acc-1", 500.0, "USD", Status.CREATED),
                        new AccountActivatedEvent("acc-1", "USD", Status.ACTIVATED));
    }

    @Test
    @DisplayName("should publish only AccountCreatedEvent when balance <= 100")
    void createAccount_withBalanceLessThan100_shouldNotActivate() {
        fixture.givenNoPriorActivity()
                .when(new CreateAccountCommand("acc-2", 50.0, "GBP"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new AccountCreatedEvent("acc-2", 50.0, "GBP", Status.CREATED));
    }

    @Test
    @DisplayName("should publish MoneyCreditedEvent when crediting money")
    void creditMoney_shouldPublishMoneyCreditedEvent() {
        fixture.given(
                        new AccountCreatedEvent("acc-3", 200.0, "EUR", Status.CREATED),
                        new AccountActivatedEvent("acc-3", "EUR", Status.ACTIVATED))
                .when(new CreditMoneyCommand("acc-3", 100.0, "EUR"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new MoneyCreditedEvent("acc-3", 100.0, "EUR"));
    }

    @Test
    @DisplayName("should publish MoneyDebitedEvent and AccountHeldEvent when balance goes negative")
    void debitMoney_whenBalanceGoesNegative_shouldHoldAccount() {
        fixture.given(
                        new AccountCreatedEvent("acc-4", 150.0, "USD", Status.CREATED),
                        new AccountActivatedEvent("acc-4", "USD", Status.ACTIVATED))
                .when(new DebitMoneyCommand("acc-4", 200.0, "USD"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        new MoneyDebitedEvent("acc-4", 200.0, "USD"),
                        new AccountHeldEvent("acc-4", Status.HOLD));
    }

    @Test
    @DisplayName("should reactivate account when credit brings negative balance to zero or above")
    void creditMoney_whenBalanceGoesFromNegativeToPositive_shouldActivate() {
        fixture.given(
                        new AccountCreatedEvent("acc-5", 50.0, "USD", Status.CREATED),
                        new MoneyDebitedEvent("acc-5", 100.0, "USD"),
                        new AccountHeldEvent("acc-5", Status.HOLD))
                .when(new CreditMoneyCommand("acc-5", 100.0, "USD"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        new AccountActivatedEvent("acc-5", Status.ACTIVATED),
                        new MoneyCreditedEvent("acc-5", 100.0, "USD"));
    }
}
