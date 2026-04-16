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
import com.miaoubich.banking.dto.BalanceRequest;
import com.miaoubich.banking.dto.CreateAccountRequest;
import com.miaoubich.banking.dto.CreateAccountResponse;
import com.miaoubich.banking.event.AccountCreatedEvent;
import com.miaoubich.banking.exception.AccountNotFoundException;
import com.miaoubich.banking.exception.InsufficientFundsException;
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
				.orElseThrow(() -> new AccountNotFoundException(accountId));
		account.setAccountStatus(newStatus);
		logger.info("Updating account number {} status to {}", account.getAccountNumber(), newStatus);
		return accountMapper.toResponse(accountRepository.save(account));
	}

	@Transactional
	public CreateAccountResponse deposit(Long accountId, BalanceRequest request) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new AccountNotFoundException(accountId));
		validateAccountActive(account);
		account.setBalance(account.getBalance().add(request.getAmount()));
		logger.info("Deposited {} to account {}, new balance: {}", request.getAmount(), account.getAccountNumber(), account.getBalance());
		return accountMapper.toResponse(accountRepository.save(account));
	}

	@Transactional
	public CreateAccountResponse withdraw(Long accountId, BalanceRequest request) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new AccountNotFoundException(accountId));
		validateAccountActive(account);
		if (account.getBalance().compareTo(request.getAmount()) < 0)
			throw new InsufficientFundsException(request.getAmount(), account.getBalance());
		account.setBalance(account.getBalance().subtract(request.getAmount()));
		logger.info("Withdrew {} from account number {}, new balance: {}", request.getAmount(), account.getAccountNumber(), account.getBalance());
		return accountMapper.toResponse(accountRepository.save(account));
	}

	private void validateAccountActive(Account account) {
		if (account.getAccountStatus() != AccountStatus.ACTIVE)
			throw new IllegalArgumentException("Account " + account.getAccountNumber() + " is not active");
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
