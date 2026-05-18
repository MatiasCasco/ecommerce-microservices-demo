package com.ecommerce.productservice.event.model;

import java.math.BigDecimal;

public class ProductUpdatedEvent extends ProductEvent {

    private String name;
    private String description;
    private BigDecimal price;

    @Override
    public String getEventType() {
        return "PRODUCT_UPDATED";
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
}
