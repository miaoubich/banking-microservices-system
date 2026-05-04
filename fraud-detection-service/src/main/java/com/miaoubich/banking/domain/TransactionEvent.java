package com.miaoubich.banking.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEvent(
    String accountId,
    String accountNumber,
    String transactionType,
    BigDecimal amount,
    BigDecimal balanceAfter,
    String userId
) {}