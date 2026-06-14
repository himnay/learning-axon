package com.learning.axon.command.service;

import com.learning.axon.shared.models.AccountCreateRequest;
import com.learning.axon.shared.models.MoneyCreditRequest;
import com.learning.axon.shared.models.MoneyDebitRequest;

import java.util.concurrent.CompletableFuture;

/**
 * GoF: Template Method — defines the algorithm skeleton; concrete impl in {@code AccountCommandServiceImpl}.
 */
public interface AccountCommandService {

    String createAccount(AccountCreateRequest request);

    CompletableFuture<String> creditMoneyToAccount(String accountId, MoneyCreditRequest request);

    String debitMoneyFromAccount(String accountId, MoneyDebitRequest request);
}
