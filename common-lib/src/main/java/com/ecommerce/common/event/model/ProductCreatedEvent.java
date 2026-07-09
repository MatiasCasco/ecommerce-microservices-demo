package com.ecommerce.common.event.model;

import com.ecommerce.common.event.constants.EventRoutingKey;
import com.ecommerce.common.event.constants.EventType;

import java.math.BigDecimal;

public class ProductCreatedEvent extends ProductEvent {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer availableStock;

    public ProductCreatedEvent() {
        super(EventType.PRODUCT_CREATED);
    }

    public Integer getAvailableStock() { return availableStock; }

    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String getRoutingKey() {
        return EventRoutingKey.PRODUCT_CREATED;
    }
}
