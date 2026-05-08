package com.miaoubich.banking.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.miaoubich.banking.domain.FraudCheckResult;
import com.miaoubich.banking.domain.FraudRiskLevel;
import com.miaoubich.banking.domain.TransactionEvent;

@Service
public class FraudDetectionService {

    public FraudCheckResult checkFraud(TransactionEvent event) {
        Set<String> rulesTriggered = new HashSet<>();

        double amount = event.amount().doubleValue();
        double balanceAfter = event.balanceAfter().doubleValue();
        String transactionType = event.transactionType();

        // Rule 1: High-value transaction
        if (amount > 10_000) {
            rulesTriggered.add("HIGH_AMOUNT_OVER_10K");
        }

        // Rule 2: High-value withdrawal
        if ("WITHDRAWAL".equalsIgnoreCase(transactionType) && amount > 5000) {
            rulesTriggered.add("HIGH_VALUE_WITHDRAWAL");
        }

        // Rule 3: Near-zero balance after withdrawal
        if ("WITHDRAWAL".equalsIgnoreCase(transactionType) && balanceAfter < 10) {
            rulesTriggered.add("CRITICAL_BALANCE_AFTER_WITHDRAWAL");
        }

        // Rule 4: Unusually large deposit (placeholder threshold)
        if ("DEPOSIT".equalsIgnoreCase(transactionType) && amount > 2000) {
            rulesTriggered.add("UNUSUALLY_LARGE_DEPOSIT");
        }

        FraudRiskLevel riskLevel;
        boolean isBlocked = false;

//        if (rulesTriggered.isEmpty()) {
//            riskLevel = FraudRiskLevel.LOW;
//        } else if (rulesTriggered.contains("HIGH_AMOUNT_OVER_10K") && rulesTriggered.contains("HIGH_VALUE_WITHDRAWAL")) {
//            riskLevel = FraudRiskLevel.FRAUD;
//            isBlocked = true;
//        } else if (rulesTriggered.contains("HIGH_AMOUNT_OVER_10K") || rulesTriggered.contains("CRITICAL_BALANCE_AFTER_WITHDRAWAL")) {
//            riskLevel = FraudRiskLevel.HIGH;
//            isBlocked = true;
//        } else {
//            riskLevel = FraudRiskLevel.MEDIUM;
//        }
        
        riskLevel = switch (rulesTriggered) {

	        case Set<String> rules when (rules.isEmpty()) ->
	            FraudRiskLevel.LOW;
	        case Set<String> rules when (rules.contains("HIGH_AMOUNT_OVER_10K")
	                                  && rules.contains("HIGH_VALUE_WITHDRAWAL")) -> {
	            isBlocked = true;
	            yield FraudRiskLevel.FRAUD;
	        }
	        case Set<String> rules when (rules.contains("HIGH_AMOUNT_OVER_10K")
	                                  || rules.contains("CRITICAL_BALANCE_AFTER_WITHDRAWAL")) -> {
	            isBlocked = true;
	            yield FraudRiskLevel.HIGH;
	        }
	        default -> FraudRiskLevel.MEDIUM;
	    };


        return new FraudCheckResult(
            event.accountId(),
            event.userId(),
            riskLevel,
            List.copyOf(rulesTriggered),
            Instant.now(),
            isBlocked
        );
    }
}
