package com.miaoubich.banking.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType;     // e.g., "ACCOUNT"
    private String aggregateId;       // e.g., accountId or accountNumber

    private String eventType;         // e.g., "ACCOUNT_CREATED"
    
    @Lob
    private String payload;           // JSON string of the event

    private LocalDateTime createdAt;

    private boolean published = false;
    
    public OutboxEvent() {}

	public OutboxEvent(String aggregateType, String aggregateId, String eventType, String payload,
			LocalDateTime createdAt, boolean published) {
		super();
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.payload = payload;
		this.createdAt = createdAt;
		this.published = published;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public void setAggregateType(String aggregateType) {
		this.aggregateType = aggregateType;
	}

	public String getAggregateId() {
		return aggregateId;
	}

	public void setAggregateId(String aggregateId) {
		this.aggregateId = aggregateId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	@Override
	public String toString() {
		return "OutboxEvent [id=" + id + ", aggregateType=" + aggregateType + ", aggregateId=" + aggregateId
				+ ", eventType=" + eventType + ", payload=" + payload + ", createdAt=" + createdAt + ", published="
				+ published + "]";
	}
    

}

/* 
 Why each field matters ?

* aggregateType  -> Helps categorize events (Account, Transaction, etc.)
* aggregateId -> Allows consumers to partition by accountId
* eventType -> Identifies the specific event (AccountCreated, AccountUpdated, etc.), Lets consumers know what happened
* payload -> The actual event data (serialized JSON), Contains all necessary info for consumers to process the event 
* published -> Marks whether Kafka publisher has sent it to Kafka, Prevents re-publishing the same event multiple times
* createdAt -> For ordering and cleanup purposes, Allows us to track when the event was created and can be used for auditing or debugging 
 
 * */
