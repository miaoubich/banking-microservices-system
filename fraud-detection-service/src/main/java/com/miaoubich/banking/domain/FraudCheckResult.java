package com.miaoubich.banking.domain;

import java.time.Instant;
import java.util.List;

public record FraudCheckResult(
		
		String transactionId,
	    String customerId,
	    FraudRiskLevel riskLevel,
	    List<String> rulesTriggered,
	    Instant checkedAt,
	    boolean isBlocked // true if you want to block immediately
) {
}
