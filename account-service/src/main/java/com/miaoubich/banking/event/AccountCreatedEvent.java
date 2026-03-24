package com.miaoubich.banking.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.miaoubich.banking.domain.AccountStatus;
import com.miaoubich.banking.domain.AccountType;

public class AccountCreatedEvent {

	private Long accountId;
	private String accountNumber;
	private BigDecimal balance;
	private AccountType accountType;
	private AccountStatus accountStatus;
	private Long clientId;
	private LocalDateTime createdAt;

	public AccountCreatedEvent() {}

	public AccountCreatedEvent(Long accountId, String accountNumber, BigDecimal balance, AccountType accountType,
			AccountStatus accountStatus, Long clientId, LocalDateTime createdAt) {
		this.accountId = accountId;
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.accountType = accountType;
		this.accountStatus = accountStatus;
		this.clientId = clientId;
		this.createdAt = createdAt;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public AccountStatus getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(AccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
