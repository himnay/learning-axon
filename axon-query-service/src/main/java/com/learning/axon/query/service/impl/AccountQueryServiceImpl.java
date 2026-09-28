package com.learning.axon.query.service.impl;

import com.learning.axon.query.entity.AccountEntity;
import com.learning.axon.query.repository.AccountRepository;
import com.learning.axon.query.service.AccountQueryService;
import com.learning.axon.shared.queries.AccountQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/**
 * GoF: Template Method — concrete query service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountQueryServiceImpl implements AccountQueryService {

    private final AccountRepository accountRepository;

    @Override
    public AccountEntity getAccount(String accountId) {
        log.info("Direct JPA lookup for account [{}]", accountId);
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account [" + accountId + "] not found"));
    }

    /** Point-to-point query handler; answers {@code null} when the read model has no such account. */
    @QueryHandler
    public AccountEntity getAccountDetails(AccountQuery query) {
        log.info("Axon point-to-point query for [{}]", query.accountNumber());
        return accountRepository.findById(query.accountNumber()).orElse(null);
    }

    /** Returns the scatter gather query. */
    @QueryHandler(queryName = "scatter-gather")
    public AccountEntity scatterGatherQuery(String accountId) {
        log.info("Scatter-gather query for [{}]", accountId);
        return accountRepository.findById(accountId).orElse(new AccountEntity());
    }
}
