package com.miaoubich.banking.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.miaoubich.banking.domain.Account;
import com.miaoubich.banking.domain.AccountStatus;
import com.miaoubich.banking.domain.OutboxEvent;
import com.miaoubich.banking.dto.CreateAccountRequest;
import com.miaoubich.banking.dto.CreateAccountResponse;
import com.miaoubich.banking.event.AccountCreatedEvent;
import com.miaoubich.banking.mapper.AccountMapper;
import com.miaoubich.banking.repository.AccountRepository;
import com.miaoubich.banking.repository.OutboxEventRepository;

import jakarta.transaction.Transactional;


@Service
public class AccountServiceImpl implements AccountService {

	private final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
	
	private final AccountRepository accountRepository;
	private final OutboxEventRepository outboxEventRepository;
	private final AccountMapper accountMapper;
	
	public AccountServiceImpl(OutboxEventRepository outboxEventRepository, AccountRepository accountRepository, AccountMapper accountMapper) {
		this.accountRepository = accountRepository;
		this.outboxEventRepository = outboxEventRepository;
		this.accountMapper = accountMapper;
	}
	
	@Transactional
	public CreateAccountResponse createAccount(CreateAccountRequest request, String clientId) {
	    Account account = accountMapper.toAccount(request);
	    account.setAccountNumber(generateAccountNumber());
	    account.setAccountStatus(AccountStatus.ACTIVE);
	    account.setClientId(clientId);
	    
	    logger.info("Creating account for clientId: {} with accountNumber: {}", clientId, account.getAccountNumber());

	    accountRepository.save(account);

	    AccountCreatedEvent event = new AccountCreatedEvent(
	        account.getId(),
	        account.getAccountNumber(),
	        account.getBalance(),
	        account.getAccountType(),
	        account.getAccountStatus(),
	        account.getClientId(),
	        account.getCreatedAt()
	    );

	    OutboxEvent outbox = new OutboxEvent(
	        "ACCOUNT",
	        account.getId().toString(),
	        "ACCOUNT_CREATED",
	        event.toString(),
	        LocalDateTime.now(),
	        false
	    );

	    outboxEventRepository.save(outbox);

	    return accountMapper.toResponse(account);
	}

	@Transactional
	public CreateAccountResponse updateAccountStatus(Long accountId, AccountStatus newStatus) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
		account.setAccountStatus(newStatus);
		logger.info("Updating account {} status to {}", accountId, newStatus);
		return accountMapper.toResponse(accountRepository.save(account));
	}

	public List<Account> getAllAccounts() {
		return accountRepository.findAll();
	}

	public List<Account> getAccountsByClientId(String clientId) {
		return accountRepository.findByClientId(clientId);
	}

	public String generateAccountNumber() {
	    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	    String random = String.format("%08d", new SecureRandom().nextInt(100_000_000));
	    return timestamp + random;
	}

}
