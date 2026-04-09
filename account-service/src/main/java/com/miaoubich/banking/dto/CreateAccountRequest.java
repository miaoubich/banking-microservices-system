package com.miaoubich.banking.dto;

import java.math.BigDecimal;

import com.miaoubich.banking.domain.AccountType;

public class CreateAccountRequest {

	private String clientId;
	private AccountType accountType;
	private BigDecimal initialBalance;

	public CreateAccountRequest() {}

	public CreateAccountRequest(String clientId, AccountType accountType, BigDecimal initialBalance) {
		this.clientId = clientId;
		this.accountType = accountType;
		this.initialBalance = initialBalance;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public BigDecimal getInitialBalance() {
		return initialBalance;
	}

	public void setInitialBalance(BigDecimal initialBalance) {
		this.initialBalance = initialBalance;
	}

	@Override
	public String toString() {
		return "CreateAccountRequest [clientId=" + clientId + ", accountType=" + accountType + ", initialBalance="
				+ initialBalance + "]";
	}
	
}
