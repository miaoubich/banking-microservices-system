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
import com.miaoubich.banking.domain.Transaction;
import com.miaoubich.banking.domain.TransactionType;
import com.miaoubich.banking.dto.BalanceRequest;
import com.miaoubich.banking.dto.CreateAccountRequest;
import com.miaoubich.banking.dto.CreateAccountResponse;
import com.miaoubich.banking.event.AccountTransactionEvent;
import com.miaoubich.banking.exception.AccountNotFoundException;
import com.miaoubich.banking.exception.InsufficientFundsException;
import com.miaoubich.banking.mapper.AccountMapper;
import com.miaoubich.banking.repository.AccountRepository;
import com.miaoubich.banking.repository.TransactionRepository;

import jakarta.transaction.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

	private final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
	
	private final AccountRepository accountRepository;
	private final AccountMapper accountMapper;
	private final TransactionRepository transactionRepository;
	private final EventPublisherService eventPublisherService;

	public AccountServiceImpl(AccountRepository accountRepository, AccountMapper accountMapper, 
							 TransactionRepository transactionRepository, EventPublisherService eventPublisherService) {
		this.accountRepository = accountRepository;
		this.accountMapper = accountMapper;
		this.transactionRepository = transactionRepository;
		this.eventPublisherService = eventPublisherService;
	}
	
	@Transactional
	public CreateAccountResponse createAccount(CreateAccountRequest request, String clientId) {
	    Account account = accountMapper.toAccount(request);
	    account.setAccountNumber(generateAccountNumber());
	    account.setAccountStatus(AccountStatus.ACTIVE);
	    account.setClientId(clientId);
	    
	    logger.info("Creating account for clientId: {} with accountNumber: {}", clientId, account.getAccountNumber());

	    accountRepository.save(account);

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
	public CreateAccountResponse deposit(Long accountId, BalanceRequest request, String clientId) {
		transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())
				.ifPresent(t -> { throw new IllegalArgumentException("Duplicate transaction: idempotency key already used"); });

		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new AccountNotFoundException(accountId));
		validateAccountOwnership(account, clientId);
		validateAccountActive(account);
		account.setBalance(account.getBalance().add(request.getAmount()));
		transactionRepository.save(new Transaction(account.getId(), 
											       account.getAccountNumber(), 
											       TransactionType.DEPOSIT, 
											       request.getAmount(), 
											       account.getBalance(), 
											       request.getIdempotencyKey())
								);

		AccountTransactionEvent event = new AccountTransactionEvent(
				account.getId().toString(), account.getAccountNumber(), "DEPOSIT",
				request.getAmount(), account.getBalance(), account.getClientId());
		eventPublisherService.saveEvent("ACCOUNT_TRANSACTION", account.getId().toString(), event);

		logger.info("Deposited {} to account {}, new balance: {}", request.getAmount(), account.getAccountNumber(), account.getBalance());
		return accountMapper.toResponse(accountRepository.save(account));
	}

	@Transactional
	public CreateAccountResponse withdraw(Long accountId, BalanceRequest request, String clientId) {
		transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())
				.ifPresent(t -> { throw new IllegalArgumentException("Duplicate transaction: idempotency key already used"); });

		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new AccountNotFoundException(accountId));
		validateAccountOwnership(account, clientId);
		validateAccountActive(account);
		if (account.getBalance().compareTo(request.getAmount()) < 0)
			throw new InsufficientFundsException(request.getAmount(), account.getBalance());
		account.setBalance(account.getBalance().subtract(request.getAmount()));
		transactionRepository.save(new Transaction(account.getId(), 
												   account.getAccountNumber(), 
												   TransactionType.WITHDRAWAL, 
												   request.getAmount(), 
												   account.getBalance(), 
												   request.getIdempotencyKey())
									);

		AccountTransactionEvent event = new AccountTransactionEvent(
				account.getId().toString(), account.getAccountNumber(), "WITHDRAWAL",
				request.getAmount(), account.getBalance(), account.getClientId());
		eventPublisherService.saveEvent("ACCOUNT_TRANSACTION", account.getId().toString(), event);

		logger.info("Withdrew {} from account number {}, new balance: {}", request.getAmount(), account.getAccountNumber(), account.getBalance());
		return accountMapper.toResponse(accountRepository.save(account));
	}

	private void validateAccountOwnership(Account account, String clientId) {
		if (!account.getClientId().equals(clientId))
			throw new IllegalArgumentException("Account " + account.getAccountNumber() + " does not belong to the requesting user");
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
		String candidate;
		do {
			String random = String.format("%08d", new SecureRandom().nextInt(100_000_000));
			candidate = timestamp + random;
		} while (accountRepository.findByAccountNumber(candidate).isPresent());
		return candidate;
	}

}
