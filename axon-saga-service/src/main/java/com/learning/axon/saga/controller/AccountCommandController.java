package com.learning.axon.saga.controller;

import com.learning.axon.saga.service.AccountCommandService;
import com.learning.axon.shared.models.AccountCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** REST endpoint — triggers the account-opening saga by creating an account aggregate. */
@RestController
@RequestMapping("/bank-accounts")
@RequiredArgsConstructor
public class AccountCommandController {

    private final AccountCommandService accountCommandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createAccount(@Valid @RequestBody AccountCreateRequest request) {
        return accountCommandService.createAccount(request);
    }
}
