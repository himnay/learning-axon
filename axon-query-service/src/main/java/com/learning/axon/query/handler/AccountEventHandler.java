package com.learning.axon.query.handler;

import com.learning.axon.query.entity.AccountEntity;
import com.learning.axon.query.repository.AccountRepository;
import com.learning.axon.shared.events.*;
import com.learning.axon.shared.notifiers.MoneyDebitNotifier;
import com.learning.axon.shared.queries.AccountDetailsQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * GoF: Observer — subscribes to domain events arriving via AMQP and maintains the read-model projection.
 *
 * <p>Processing group "amqpEvents" is wired to the AMQP message source in application.yml.
 */
@Slf4j
@Component
@ProcessingGroup("amqpEvents")
@RequiredArgsConstructor
public class AccountEventHandler {

    private final AccountRepository accountRepository;
    private final QueryUpdateEmitter queryUpdateEmitter;

    @EventHandler
    public void on(AccountCreatedEvent event) {
        log.info("Handling AccountCreatedEvent [{}]", event);
        AccountEntity entity = accountRepository.findById(event.getId())
                .orElse(AccountEntity.builder().id(event.getId()).build());
        entity.setAccountBalance(event.getAccountBalance());
        entity.setCurrency(event.getCurrency());
        entity.setStatus(event.getStatus());
        accountRepository.save(entity);
    }

    @EventHandler
    public void on(AccountActivatedEvent event) {
        log.info("Handling AccountActivatedEvent [{}]", event);
        AccountEntity entity = findOrThrow(event.getId());
        entity.setStatus(event.getStatus());
        if (event.getCurrency() != null) {
            entity.setCurrency(event.getCurrency());
        }
        accountRepository.save(entity);
    }

    @EventHandler
    public void on(AccountHeldEvent event) {
        log.info("Handling AccountHeldEvent [{}]", event);
        AccountEntity entity = findOrThrow(event.getId());
        entity.setStatus(event.getStatus());
        accountRepository.save(entity);
    }

    @EventHandler
    public void on(MoneyCreditedEvent event) {
        log.info("Handling MoneyCreditedEvent [{}]", event);
        AccountEntity entity = findOrThrow(event.getId());
        entity.setAccountBalance(entity.getAccountBalance() + event.getCreditAmount());
        entity.setCurrency(event.getCurrency());
        accountRepository.save(entity);

        // Notify subscription query subscribers
        queryUpdateEmitter.emit(
                AccountDetailsQuery.class,
                query -> query.id().equals(event.getId()),
                entity);
    }

    @EventHandler
    public void on(MoneyDebitedEvent event) {
        log.info("Handling MoneyDebitedEvent [{}]", event);
        AccountEntity entity = findOrThrow(event.getId());
        entity.setAccountBalance(entity.getAccountBalance() - event.getDebitAmount());
        entity.setCurrency(event.getCurrency());
        accountRepository.save(entity);
    }

    // ── Query handlers ────────────────────────────────────────────────────────

    @QueryHandler
    public List<AccountEntity> handle(AccountDetailsQuery query) {
        return accountRepository.findAll();
    }

    @QueryHandler(queryName = "scatter-gather")
    public AccountEntity handleScatterGather(String accountId) {
        AccountEntity entity = accountRepository.findById(accountId).orElse(new AccountEntity());
        entity.setAccountBalance(entity.getAccountBalance() + 10);
        return entity;
    }

    @QueryHandler
    public AccountEntity handle(MoneyDebitNotifier notifier) {
        return accountRepository.findById(notifier.id()).orElse(new AccountEntity());
    }

    @ResetHandler
    public void onReset() {
        log.info("Resetting query-side projection — clearing account_details table");
        accountRepository.deleteAll();
    }

    private AccountEntity findOrThrow(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Account [" + id + "] not found in read-model"));
    }
}
