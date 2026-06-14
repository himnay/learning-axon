package com.learning.axon.query.handler;

import com.learning.axon.query.entity.AccountEntity;
import com.learning.axon.query.repository.AccountRepository;
import com.learning.axon.shared.enums.Status;
import com.learning.axon.shared.events.AccountCreatedEvent;
import com.learning.axon.shared.events.MoneyCreditedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.axonframework.queryhandling.QueryUpdateEmitter;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountEventHandler Unit Tests")
class AccountEventHandlerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private QueryUpdateEmitter queryUpdateEmitter;

    @InjectMocks
    private AccountEventHandler handler;

    @Test
    @DisplayName("should create account entity on AccountCreatedEvent")
    void on_accountCreatedEvent_shouldSaveEntity() {
        var event = new AccountCreatedEvent("acc-1", 500.0, "USD", Status.CREATED);
        when(accountRepository.findById("acc-1")).thenReturn(Optional.empty());

        handler.on(event);

        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    @DisplayName("should update balance and emit subscription update on MoneyCreditedEvent")
    void on_moneyCreditedEvent_shouldUpdateAndEmit() {
        var existing = AccountEntity.builder()
                .id("acc-1").accountBalance(100.0).currency("USD").status(Status.ACTIVATED).build();
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(existing));

        var event = new MoneyCreditedEvent("acc-1", 50.0, "USD");
        handler.on(event);

        verify(accountRepository).save(existing);
        verify(queryUpdateEmitter).emit(any(), any(), any(AccountEntity.class));
    }
}
