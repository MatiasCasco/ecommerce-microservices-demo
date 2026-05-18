package com.ecommerce.productservice.event.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class ProductEvent {

    private UUID eventId;
    private LocalDateTime occurredAt;
    private Long productId;

    public abstract String getEventType();

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
