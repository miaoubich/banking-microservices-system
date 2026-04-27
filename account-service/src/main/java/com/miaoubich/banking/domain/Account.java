package com.miaoubich.banking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true)
	private String accountNumber;
	@Column(nullable = false)
	private BigDecimal balance;
	@Enumerated(EnumType.STRING)
	private AccountType AccountType;
	@Enumerated(EnumType.STRING)
	private AccountStatus accountStatus;
	@CreationTimestamp
	private LocalDateTime createdAt;
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	@Column(nullable = false)
	private String clientId;
	
	@Version
	private Long version;
	
	public Account() {}
	
	public Account(String accountNumber, BigDecimal balance, AccountType accountType, AccountStatus accountStatus, LocalDateTime createdAt,
			LocalDateTime updatedAt, String clientId, Long version) {
		this.accountNumber = accountNumber;
		this.balance = balance;
		AccountType = accountType;
		this.accountStatus = accountStatus;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.clientId = clientId;
		this.version = version;
	}

	public Long getId() {
		return id;
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
		return AccountType;
	}

	public void setAccountType(AccountType accountType) {
		AccountType = accountType;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public AccountStatus getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(AccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
	
	@Override
	public String toString() {
		return "Account [id=" + id + ", accountNumber=" + accountNumber + ", balance=" + balance + ", AccountType="
				+ AccountType + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", accountStatus="
				+ accountStatus + ", clientId=" + clientId + ", version=" + version +"]";
	}
	
}
