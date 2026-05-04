package com.miaoubich.banking.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AccountTransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(AccountTransactionListener.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AccountTransactionListener() {
        logger.info("🚀 AccountTransactionListener initialized - ready to consume events from banking.account-transaction");
    }

    @KafkaListener(
    		topics = "${spring.kafka.template.default-topic}", 
    		groupId = "${spring.kafka.consumer.group-id}"
    		)
    public void handleAccountTransaction(String message) {
        logger.info("📥 Received message: {}", message);
        try {
            JsonNode event = objectMapper.readTree(message);
            
            String accountNumber = event.get("accountNumber").asText();
            String transactionType = event.get("transactionType").asText();
            double amount = event.get("amount").asDouble();
            double balanceAfter = event.get("balanceAfter").asDouble();
            
            logger.info("🔔 NOTIFICATION: Account {} {} of ${} completed. New balance: ${}", 
                       accountNumber, transactionType.toLowerCase(), amount, balanceAfter);
            
            sendNotification(accountNumber, transactionType, amount, balanceAfter);
            
        } catch (Exception e) {
            logger.error("❌ Failed to process account transaction event: {}", message, e);
        }
    }
    
    private void sendNotification(String accountNumber, String transactionType, double amount, double balance) {
        // Simulate notification sending
        logger.info("📧 Sending notification: Your account {} has been {} with ${}", 
                   accountNumber, transactionType.toLowerCase(), amount);
    }
}