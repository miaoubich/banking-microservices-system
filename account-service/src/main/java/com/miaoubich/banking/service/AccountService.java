package com.miaoubich.banking.service;

import com.miaoubich.banking.domain.AccountStatus;
import com.miaoubich.banking.dto.CreateAccountRequest;
import com.miaoubich.banking.dto.CreateAccountResponse;

public interface AccountService {

	CreateAccountResponse createAccount(CreateAccountRequest request, Long clientId);
	CreateAccountResponse updateAccountStatus(Long accountId, AccountStatus newStatus);
}
