package com.miaoubich.banking.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.miaoubich.banking.domain.Account;
import com.miaoubich.banking.domain.AccountStatus;
import com.miaoubich.banking.domain.OutboxEvent;

import jakarta.transaction.Transactional;


@Service
public class AccountServiceImpl implements AccountService {

	private final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
	
	@Transactional
	public Account createAccount(CreateAccountRequest request) {
	    Account account = new Account();
	    account.setAccountNumber(generateNumber());
	    account.setBalance(BigDecimal.ZERO);
	    account.setAccountType(request.getType());
	    account.setAccountStatus(AccountStatus.ACTIVE);

	    accountRepository.save(account);

	    AccountCreatedEvent event = new AccountCreatedEvent(
	        account.getId(),
	        account.getAccountNumber(),
	        account.getAccountType(),
	        account.getCreatedAt()
	    );

	    OutboxEvent outbox = new OutboxEvent(
	        "ACCOUNT",
	        account.getId().toString(),
	        "ACCOUNT_CREATED",
	        objectMapper.writeValueAsString(event)
	    );

	    outboxRepository.save(outbox);

	    return account;
	}
	
}
