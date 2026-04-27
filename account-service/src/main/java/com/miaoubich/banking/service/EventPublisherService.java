package com.miaoubich.banking.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoubich.banking.domain.OutboxEvent;
import com.miaoubich.banking.repository.OutboxEventRepository;

@Service
public class EventPublisherService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisherService.class);
    
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventPublisherService(OutboxEventRepository outboxEventRepository, 
                               KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveEvent(String eventType, String aggregateId, Object eventData) {
        try {
            String payload = objectMapper.writeValueAsString(eventData);
            OutboxEvent event = new OutboxEvent(eventType, aggregateId, payload);
            outboxEventRepository.save(event);
            logger.info("Saved outbox event: {} for aggregate: {}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event data", e);
            throw new RuntimeException("Failed to save event", e);
        }
    }

    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
        
        for (OutboxEvent event : pendingEvents) {
            try {
                String topicName = "banking." + event.getEventType().toLowerCase().replace("_", "-");
                kafkaTemplate.send(topicName, event.getAggregateId(), event.getPayload());
                
                event.setProcessed(true);
                outboxEventRepository.save(event);
                
                logger.info("Published event {} to topic {}", event.getId(), topicName);
            } catch (Exception e) {
                logger.error("Failed to publish event {}", event.getId(), e);
            }
        }
    }
}