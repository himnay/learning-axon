package com.learning.axon.saga.service;

import com.learning.axon.shared.models.AccountCreateRequest;

/** GoF: Template Method — defines the account-creation contract for the saga service. */
public interface AccountCommandService {

    String createAccount(AccountCreateRequest request);
}
