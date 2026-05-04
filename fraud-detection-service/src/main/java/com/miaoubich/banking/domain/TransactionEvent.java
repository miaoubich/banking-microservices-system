package com.miaoubich.banking.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEvent(
 String transactionId,
 String accountId,
 String customerId,
 BigDecimal amount,
 String currency,
 String channel, // "web", "mobile", "atm", ...
 String location, // "city:country"
 Instant timestamp,
 String ip,
 String userAgent
) {}