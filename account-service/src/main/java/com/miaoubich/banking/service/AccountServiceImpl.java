package com.miaoubich.banking.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.miaoubich.banking.constants.Constants;
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
	@Override
	public CreateAccountResponse createAccount(CreateAccountRequest request, String clientId) {
		Account account = accountMapper.toAccount(request);
		account.setAccountNumber(generateAccountNumber());
		account.setAccountStatus(AccountStatus.ACTIVE);
		account.setClientId(clientId);

		logger.info("Creating account for clientId: {} with accountNumber: {}", clientId, account.getAccountNumber());

		accountRepository.save(account);

		return accountMapper.toResponse(account);
	}
	
	@Override
	public List<CreateAccountResponse> getAllAccounts() {
		return accountRepository.findAll().stream()
				.map(accountMapper::toResponse)
				.toList();
	}

	@Override
	@Cacheable(value = "accounts", key = "#clientId")
	public List<CreateAccountResponse> getAccountsByClientId(String clientId) {
		return accountRepository.findByClientId(clientId).stream()
				.map(accountMapper::toResponse)
				.toList();
	}

	@Transactional
	@Override
	@Caching(evict = {
			@CacheEvict(value = "accountById", key = "#accountId"),
			@CacheEvict(value = "accountByClient", key = "#clientId")
			})
	public CreateAccountResponse deposit(Long accountId, BalanceRequest request, String clientId) {
		Account account = findAccountByAccountId(accountId);
		validateAccountOwnership(account, clientId);
		validateAccountActive(account);
		validateNoDuplicateTransaction(account.getId(), request.getAmount(), TransactionType.DEPOSIT);

		account.setBalance(account.getBalance().add(request.getAmount()));
		transactionRepository.save(new Transaction(account.getId(),
				account.getAccountNumber(),
				TransactionType.DEPOSIT,
				request.getAmount(),
				account.getBalance(),
				UUID.randomUUID().toString()));
		Account savedAccount = accountRepository.save(account);

		AccountTransactionEvent event = new AccountTransactionEvent(
				account.getId().toString(), account.getAccountNumber(), Constants.DEPOSIT,
				request.getAmount(), account.getBalance(), account.getClientId());
		eventPublisherService.saveEvent(Constants.ACCOUNT_TRANSACTION, account.getId().toString(), event);

		logger.info("Deposited {} to account {}, new balance: {}", request.getAmount(), account.getAccountNumber(), account.getBalance());
		return accountMapper.toResponse(savedAccount);
	}

	@Transactional
	@Override
	@Caching(evict = {
		@CacheEvict(value = "accountById", key = "#accountId"),
		@CacheEvict(value = "accountByClient", key = "#clientId")
		})
	public CreateAccountResponse withdraw(Long accountId, BalanceRequest request, String clientId) {
		Account account = findAccountByAccountId(accountId);
		validateAccountOwnership(account, clientId);
		validateAccountActive(account);
		validateNoDuplicateTransaction(account.getId(), request.getAmount(), TransactionType.WITHDRAWAL);

		if (account.getBalance().compareTo(request.getAmount()) < 0)
			throw new InsufficientFundsException(request.getAmount(), account.getBalance());
		account.setBalance(account.getBalance().subtract(request.getAmount()));
		transactionRepository.save(new Transaction(account.getId(),
				account.getAccountNumber(),
				TransactionType.WITHDRAWAL,
				request.getAmount(),
				account.getBalance(),
				UUID.randomUUID().toString()));

		AccountTransactionEvent event = new AccountTransactionEvent(
				account.getId().toString(), account.getAccountNumber(), Constants.WITHDRAWAL,
				request.getAmount(), account.getBalance(), account.getClientId());
		eventPublisherService.saveEvent(Constants.ACCOUNT_TRANSACTION, account.getId().toString(), event);

		logger.info("Withdrew {} from account number {}, new balance: {}", request.getAmount(), account.getAccountNumber(), account.getBalance());
		return accountMapper.toResponse(accountRepository.save(account));
	}

	private void validateNoDuplicateTransaction(Long accountId, BigDecimal amount, TransactionType type) {
		LocalDateTime window = LocalDateTime.now().minusSeconds(30);
		boolean duplicate = transactionRepository
				.existsByAccountIdAndAmountAndTypeAndCreatedAtAfter(accountId, amount, type, window);
		if (duplicate)
			throw new IllegalArgumentException("Duplicate transaction: same amount and type submitted within 30 seconds");
	}

	@Override
	@Cacheable(value= "accounts", key = "#accountId")
	public Account findAccountByAccountId(Long accountId) {
		return accountRepository.findById(accountId)
				.orElseThrow(() -> new AccountNotFoundException(accountId));
	}

	private void validateAccountOwnership(Account account, String clientId) {
		if (!account.getClientId().equals(clientId))
			throw new IllegalArgumentException("Account " + account.getAccountNumber() + " does not belong to the requesting user");
	}

	private void validateAccountActive(Account account) {
		if (account.getAccountStatus() != AccountStatus.ACTIVE)
			throw new IllegalArgumentException("Account " + account.getAccountNumber() + " is not active");
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
	
	@Transactional
	@Override
	@CacheEvict(value ="accounts", key = "#id")
	public CreateAccountResponse updateAccountStatus(Long accountId, AccountStatus newStatus) {
		Account account = findAccountByAccountId(accountId);
		account.setAccountStatus(newStatus);
		logger.info("Updating account number {} status to {}", account.getAccountNumber(), newStatus);
		return accountMapper.toResponse(accountRepository.save(account));
	}
	
	@CacheEvict(value = "users", allEntries = true)
	public void clearUsersCache() {}

}
