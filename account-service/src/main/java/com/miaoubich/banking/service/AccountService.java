package com.miaoubich.banking.service;

import java.util.List;

import com.miaoubich.banking.domain.Account;
import com.miaoubich.banking.domain.AccountStatus;
import com.miaoubich.banking.dto.CreateAccountRequest;
import com.miaoubich.banking.dto.CreateAccountResponse;

public interface AccountService {

	CreateAccountResponse createAccount(CreateAccountRequest request, String clientId);
	CreateAccountResponse updateAccountStatus(Long accountId, AccountStatus newStatus);
	List<Account> getAllAccounts();
	List<Account> getAccountsByClientId(String clientId);
}
