package com.miaoubich.banking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.miaoubich.banking.domain.TransactionType;

public class TransactionResponse {

    private Long id;
    private String accountNumber;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;

    public TransactionResponse() {}

    public TransactionResponse(Long id, String accountNumber, TransactionType type, BigDecimal amount, BigDecimal balanceAfter, LocalDateTime createdAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
