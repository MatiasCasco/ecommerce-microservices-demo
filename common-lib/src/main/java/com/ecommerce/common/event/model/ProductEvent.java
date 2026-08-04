package com.ecommerce.common.event.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class ProductEvent {

    private UUID eventId;
    private String eventType;
    private String eventVersion;
    private String traceId;
    private LocalDateTime occurredAt;
    private Long aggregateId ;

    protected ProductEvent(String eventType) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.eventVersion = "1.0";
        this.occurredAt = LocalDateTime.now();
    }

    public abstract String getRoutingKey();

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventVersion() {
        return eventVersion;
    }

    public String getTraceId() {
        return traceId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public Long getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(Long aggregateId) {
        this.aggregateId = aggregateId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
