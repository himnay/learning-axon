package com.learning.axon.query.controller;

import com.learning.axon.query.entity.AccountEntity;
import com.learning.axon.query.service.AccountQueryService;
import com.learning.axon.shared.notifiers.MoneyDebitNotifier;
import com.learning.axon.shared.queries.AccountDetailsQuery;
import com.learning.axon.shared.queries.AccountQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.GenericQueryMessage;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryResponseMessage;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * GoF: Chain of Responsibility — REST → QueryGateway → QueryBus → QueryHandler.
 * Demonstrates point-to-point, subscription, and scatter-gather Axon query patterns.
 */
@Slf4j
@RestController
@RequestMapping("/bank-accounts")
@RequiredArgsConstructor
public class AccountQueryController {

    private final AccountQueryService accountQueryService;
    private final QueryGateway queryGateway;
    private final QueryBus queryBus;

    /** Direct JPA lookup (bypasses Axon query bus). */
    @GetMapping("/{accountId}")
    public AccountEntity getAccount(@PathVariable String accountId) {
        return accountQueryService.getAccount(accountId);
    }

    /** Axon point-to-point query — routes via QueryGateway to a single @QueryHandler. */
    @GetMapping("/{accountId}/details")
    public AccountEntity getAccountViaQuery(@PathVariable String accountId)
            throws ExecutionException, InterruptedException {
        return queryGateway.query(new AccountQuery(accountId), ResponseTypes.instanceOf(AccountEntity.class)).get();
    }

    /**
     * Axon subscription query — streams real-time updates whenever a credit occurs.
     * GoF: Observer — subscribes and pushes updates over a reactive stream.
     */
    @GetMapping(value = "/notify/credit/{accountId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AccountEntity> subscribeToCredits(@PathVariable String accountId) {
        SubscriptionQueryResult<List<AccountEntity>, AccountEntity> result = queryGateway.subscriptionQuery(
                new AccountDetailsQuery(accountId, 0, Integer.MAX_VALUE),
                ResponseTypes.multipleInstancesOf(AccountEntity.class),
                ResponseTypes.instanceOf(AccountEntity.class));

        result.initialResult().subscribe(list -> list.forEach(e -> log.info("Initial: {}", e)));
        return result.updates().doOnNext(e -> log.info("Update: {}", e));
    }

    /**
     * Axon subscription query — streams real-time updates whenever a debit occurs.
     */
    @GetMapping(value = "/notify/debit/{accountId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AccountEntity> subscribeToDebits(@PathVariable String accountId) {
        return queryGateway.subscriptionQuery(
                new MoneyDebitNotifier(accountId),
                ResponseTypes.instanceOf(AccountEntity.class),
                ResponseTypes.instanceOf(AccountEntity.class))
                .updates();
    }

    /**
     * Axon scatter-gather query — fan-out to multiple handlers, collect results.
     */
    @GetMapping("/scatter/{accountId}")
    public Stream<QueryResponseMessage<AccountEntity>> scatterGather(@PathVariable String accountId) {
        GenericQueryMessage<String, AccountEntity> query =
                new GenericQueryMessage<>(accountId, "scatter-gather", ResponseTypes.instanceOf(AccountEntity.class));
        return queryBus.scatterGather(query, 10, TimeUnit.SECONDS);
    }
}
