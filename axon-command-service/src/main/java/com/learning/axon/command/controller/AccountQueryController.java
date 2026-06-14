package com.learning.axon.command.controller;

import com.learning.axon.command.service.AccountQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Reads raw events from the Axon event store — useful for debugging and admin purposes. */
@RestController
@RequestMapping("/bank-accounts")
@RequiredArgsConstructor
public class AccountQueryController {

    private final AccountQueryService accountQueryService;

    @GetMapping("/{accountId}/events")
    public List<Object> listEvents(@PathVariable String accountId) {
        return accountQueryService.listEventsForAccount(accountId);
    }
}
