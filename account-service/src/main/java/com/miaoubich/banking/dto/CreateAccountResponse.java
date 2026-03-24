package com.miaoubich.banking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.miaoubich.banking.domain.AccountStatus;
import com.miaoubich.banking.domain.AccountType;

public class CreateAccountResponse {

	private Long id;
	private String accountNumber;
	private BigDecimal balance;
	private AccountType accountType;
	private AccountStatus accountStatus;
	private Long clientId;
	private LocalDate createdAt;

	public CreateAccountResponse() {}

	public CreateAccountResponse(Long id, String accountNumber, BigDecimal balance, AccountType accountType,
			AccountStatus accountStatus, Long clientId, LocalDate createdAt) {
		this.id = id;
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.accountType = accountType;
		this.accountStatus = accountStatus;
		this.clientId = clientId;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}
}
