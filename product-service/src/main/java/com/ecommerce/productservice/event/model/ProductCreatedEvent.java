package com.ecommerce.productservice.event.model;

import com.ecommerce.productservice.event.constants.EventConstants;
import com.ecommerce.productservice.event.constants.EventRoutingKey;
import com.ecommerce.productservice.event.constants.EventType;

import java.math.BigDecimal;

public class ProductCreatedEvent extends ProductEvent {

    private String name;
    private String description;
    private BigDecimal price;

    public ProductCreatedEvent() {
        super(EventType.PRODUCT_CREATED);
    }

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
