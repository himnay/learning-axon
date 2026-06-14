package com.learning.axon.command.service.impl;

import com.learning.axon.command.service.AccountCommandService;
import com.learning.axon.shared.commands.CreateAccountCommand;
import com.learning.axon.shared.commands.CreditMoneyCommand;
import com.learning.axon.shared.commands.DebitMoneyCommand;
import com.learning.axon.shared.models.AccountCreateRequest;
import com.learning.axon.shared.models.MoneyCreditRequest;
import com.learning.axon.shared.models.MoneyDebitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * GoF: Template Method — concrete implementation that dispatches commands via CommandGateway.
 * GoF: Chain of Responsibility — CommandGateway → CommandBus → CommandHandler.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCommandServiceImpl implements AccountCommandService {

    private final CommandGateway commandGateway;

    @Override
    public String createAccount(AccountCreateRequest request) {
        String accountId = UUID.randomUUID().toString();
        log.info("Creating account [{}] with balance [{}]", accountId, request.startingBalance());
        return commandGateway.sendAndWait(
                new CreateAccountCommand(accountId, request.startingBalance(), request.currency()));
    }

    @Override
    public CompletableFuture<String> creditMoneyToAccount(String accountId, MoneyCreditRequest request) {
        log.info("Crediting [{}] {} to account [{}]", request.creditAmount(), request.currency(), accountId);
        return commandGateway.send(
                new CreditMoneyCommand(accountId, request.creditAmount(), request.currency()));
    }

    @Override
    public String debitMoneyFromAccount(String accountId, MoneyDebitRequest request) {
        log.info("Debiting [{}] {} from account [{}]", request.debitAmount(), request.currency(), accountId);
        return commandGateway.sendAndWait(
                new DebitMoneyCommand(accountId, request.debitAmount(), request.currency()));
    }
}
