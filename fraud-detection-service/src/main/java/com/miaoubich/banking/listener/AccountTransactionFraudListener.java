package com.miaoubich.banking.listener;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoubich.banking.domain.FraudCheckResult;
import com.miaoubich.banking.domain.TransactionEvent;
import com.miaoubich.banking.service.FraudDetectionService;

@Component
public class AccountTransactionFraudListener {

    private static final Logger logger = LoggerFactory.getLogger(AccountTransactionFraudListener.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FraudDetectionService fraudDetectionService;

    public AccountTransactionFraudListener(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @KafkaListener(
        topics = "banking.account-transaction",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleAccountTransactionForFraud(String message, Acknowledgment ack) {
        logger.info("🔍 Fraud check: Received transaction event: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);

            TransactionEvent event = new TransactionEvent(
                node.get("accountId").asText(),
                node.get("accountNumber").asText(),
                node.get("transactionType").asText(),
                new BigDecimal(node.get("amount").asText()),
                new BigDecimal(node.get("balanceAfter").asText()),
                node.get("userId").asText()
            );

            FraudCheckResult result = fraudDetectionService.checkFraud(event);

            logger.info("✅ FraudCheckResult: riskLevel={}, isBlocked={}, rules={} for account {}",
                result.riskLevel(), result.isBlocked(), result.rulesTriggered(), event.accountNumber());

            ack.acknowledge();
        } catch (Exception e) {
            logger.error("❌ Failed to process account transaction event for fraud: {}", message, e);
        }
    }
}
