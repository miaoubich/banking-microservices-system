package com.miaoubich.banking.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.miaoubich.banking.domain.FraudCheckResult;
import com.miaoubich.banking.domain.FraudRiskLevel;
import com.miaoubich.banking.domain.TransactionEvent;

@Service
public class FraudDetectionService {

 public FraudCheckResult checkFraud(TransactionEvent event) {
	 
     List<String> rulesTriggered = new ArrayList<>();

     // Rule 1: very high amount
     if (event.amount().compareTo(BigDecimal.valueOf(10_000)) > 0) {
         rulesTriggered.add("HIGH_AMOUNT");
     }

     // Rule 2: from unusual location
     if (isUnusualCountry(event.customerId(), event.location())) {
         rulesTriggered.add("UNUSUAL_LOCATION");
     }

     // Rule 3: too many transactions in last N minutes
     long countsLast5min = countRecentTransactions(event.customerId(), Duration.ofMinutes(5));
     if (countsLast5min > 10) {
         rulesTriggered.add("HIGH_VELOCITY");
     }

     // Translate to risk level
     if (rulesTriggered.isEmpty()) {
         return new FraudCheckResult(
             event.transactionId(),
             event.customerId(),
             FraudRiskLevel.LOW,
             List.of(),
             Instant.now(),
             false
         );
     }

     boolean isBlocked = rulesTriggered.stream()
         .anyMatch(r -> r.equals("HIGH_AMOUNT") || r.equals("UNUSUAL_LOCATION"));

     FraudRiskLevel riskLevel =
         isBlocked ? FraudRiskLevel.FRAUD : FraudRiskLevel.HIGH;

     return new FraudCheckResult(
         event.transactionId(),
         event.customerId(),
         riskLevel,
         rulesTriggered,
         Instant.now(),
         isBlocked
     );
 }

 private boolean isUnusualCountry(String customerId, String location) {
     // Dummy: in real system fetch customer’s usual locations from DB / Redis
     return !location.equals("Zagreb:Croatia");
 }

 private long countRecentTransactions(String customerId, Duration lookback) {
     // Dummy: in real system query DB or keep a rolling window in memory / Kafka Streams
     return 0;
 }
}
