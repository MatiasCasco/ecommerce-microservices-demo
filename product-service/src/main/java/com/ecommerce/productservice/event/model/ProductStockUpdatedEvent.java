package com.ecommerce.productservice.event.model;

import com.ecommerce.productservice.event.constants.EventRoutingKey;
import com.ecommerce.productservice.event.constants.EventType;

public class ProductStockUpdatedEvent extends ProductEvent {

    private Integer stock;

    public ProductStockUpdatedEvent() {
        super(EventType.PRODUCT_STOCK_UPDATED);
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    @Override
    public String getRoutingKey() {
        return EventRoutingKey.PRODUCT_STOCK_UPDATED;
    }
}
