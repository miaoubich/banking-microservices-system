package com.miaoubich.banking.listener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoubich.banking.domain.FraudCheckResult;
import com.miaoubich.banking.domain.FraudRiskLevel;

@Component
public class AccountTransactionFraudListener {

    private static final Logger logger = LoggerFactory.getLogger(AccountTransactionFraudListener.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AccountTransactionFraudListener() {
        logger.info("🏗️ AccountTransactionFraudListener initialized - consuming from banking.account-transaction");
    }

    @KafkaListener(
        topics = "banking.account-transaction",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleAccountTransactionForFraud(String message) {
        logger.info("🔍 Fraud check: Received transaction event: {}", message);
        try {
            JsonNode event = objectMapper.readTree(message);

            String accountId = event.get("accountId").asText();
            String accountNumber = event.get("accountNumber").asText();
            String transactionType = event.get("transactionType").asText();
            double amount = event.get("amount").asDouble();
            double balanceAfter = event.get("balanceAfter").asDouble();
            String clientId = event.get("clientId").asText();

            // Run your fraud rules here
            FraudCheckResult result = runFraudRules(
                clientId, accountId, accountNumber, transactionType, amount, balanceAfter);

            logger.info("✅ FraudCheckResult: {} for account {}", result, accountNumber);

            // Optional: send to another topic (e.g., fraud.alerts) or persist for audit
        } catch (Exception e) {
            logger.error("❌ Failed to process account transaction event for fraud: {}", message, e);
        }
    }

    private FraudCheckResult runFraudRules(
    	    String clientId,
    	    String accountId,
    	    String accountNumber,
    	    String transactionType,
    	    double amount,
    	    double balanceAfter
    	) {
    	    List<String> rulesTriggered = new ArrayList<>();

    	    // Rule 1: High‑value transaction
    	    if (amount > 10_000) {
    	        rulesTriggered.add("HIGH_AMOUNT_OVER_10K");
    	    }

    	    // Rule 2: Withdrawal on a very high amount
    	    if ("WITHDRAWAL".equalsIgnoreCase(transactionType) && amount > 5_000) {
    	        rulesTriggered.add("HIGH_VALUE_WITHDRAWAL");
    	    }

    	    // Rule 3: Negative or near zero balance after withdrawal (high risk)
    	    if ("WITHDRAWAL".equalsIgnoreCase(transactionType) && balanceAfter < 10) {
    	        rulesTriggered.add("CRITICAL_BALANCE_AFTER_WITHDRAWAL");
    	    }

    	    // Rule 4: Deposit larger than usual – dummy pattern (you’d fetch historical avg)
    	    double usualMaxDeposit = 1_000; // in real system: load from DB / cache
    	    if ("DEPOSIT".equalsIgnoreCase(transactionType) && amount > usualMaxDeposit * 2) {
    	        rulesTriggered.add("UNUSUALLY_LARGE_DEPOSIT");
    	    }

    	    // Infer risk level
    	    FraudRiskLevel riskLevel;
    	    boolean isBlocked = false;

    	    if (rulesTriggered.isEmpty()) {
    	        riskLevel = FraudRiskLevel.LOW;
    	    } else if (rulesTriggered.stream()
    	        .anyMatch(r -> r.contains("EXTREME_HIGH_AMOUNT") || r.contains("EXTREME_HIGH_WITHDRAWAL"))) {
    	        // Very extreme amount → treat as confirmed or near‑confirmed fraud
    	        riskLevel = FraudRiskLevel.FRAUD;
    	        isBlocked = true;
    	    } else if (rulesTriggered.stream()
    	        .anyMatch(r -> r.contains("HIGH_AMOUNT") || r.contains("CRITICAL") || r.contains("EXTREMELY_UNUSUAL_LARGE_DEPOSIT"))) {
    	        // Still very suspicious, but not as extreme
    	        riskLevel = FraudRiskLevel.HIGH;
    	        isBlocked = true;
    	    } else {
    	        // Some rules match but not extreme ones
    	        riskLevel = FraudRiskLevel.MEDIUM;
    	    }

    	    return new FraudCheckResult(
    	        accountId,            // transactionId = accountId (or you can use a real txId)
    	        clientId,             // customerId
    	        riskLevel,            // riskLevel
    	        List.copyOf(rulesTriggered), // defensive copy
    	        Instant.now(),        // checkedAt
    	        isBlocked             // isBlocked
    	    );
    	}
}
