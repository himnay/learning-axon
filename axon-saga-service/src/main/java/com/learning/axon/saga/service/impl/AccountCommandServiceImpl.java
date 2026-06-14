package com.learning.axon.saga.service.impl;

import com.learning.axon.saga.service.AccountCommandService;
import com.learning.axon.shared.commands.CreateAccountCommand;
import com.learning.axon.shared.models.AccountCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * GoF: Template Method — concrete implementation.
 * GoF: Chain of Responsibility — sends commands via CommandGateway → CommandBus.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCommandServiceImpl implements AccountCommandService {

    private final CommandGateway commandGateway;

    @Override
    public String createAccount(AccountCreateRequest request) {
        String accountId = UUID.randomUUID().toString();
        log.info("Saga service creating account [{}]", accountId);
        return commandGateway.sendAndWait(
                new CreateAccountCommand(accountId, request.startingBalance(), request.currency()));
    }
}
