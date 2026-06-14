package com.learning.axon.query.service;

import com.learning.axon.query.entity.AccountEntity;
import com.learning.axon.shared.queries.AccountQuery;

/** GoF: Template Method — skeleton for query-side account lookups. */
public interface AccountQueryService {

    AccountEntity getAccount(String accountId);

    AccountEntity getAccountDetails(AccountQuery query);
}
