package com.miaoubich.banking.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AccountTransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(AccountTransactionListener.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(
            topics = "${spring.kafka.template.default-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleAccountTransaction(String message, Acknowledgment ack) {
        logger.info("📥 Received message: {}", message);
        try {
            JsonNode event = objectMapper.readTree(message);

            String accountNumber = event.get("accountNumber").asText();
            String transactionType = event.get("transactionType").asText();
            double amount = event.get("amount").asDouble();
            double balanceAfter = event.get("balanceAfter").asDouble();
            String userId = event.get("userId").asText();

            logger.info("NOTIFICATION: Account {} {} of ${} completed. New balance: ${}",
                    accountNumber, transactionType.toLowerCase(), amount, balanceAfter);

            sendNotification(userId, accountNumber, transactionType, amount, balanceAfter);

            //Set 'enable-auto-commit: false' in 'yml file'
            // commit and move the offset to upper number only if the event is delivered otherwise 
            // ack is NOT called → Kafka will redeliver this message on restart
            ack.acknowledge();
        } catch (Exception e) {
            logger.error("❌ Failed to process account transaction event: {}", message, e);
        }
    }

    private void sendNotification(String userId, String accountNumber, String transactionType, double amount, double balance) {
        logger.info("Sending notification to user {}: account {} has been {} with ${}",
                userId, accountNumber, transactionType.toLowerCase(), amount);
    }
}