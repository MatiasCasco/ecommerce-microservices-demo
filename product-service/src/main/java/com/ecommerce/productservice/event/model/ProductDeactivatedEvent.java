package com.ecommerce.productservice.event.model;

import com.ecommerce.productservice.event.constants.EventRoutingKey;
import com.ecommerce.productservice.event.constants.EventType;

public class ProductDeactivatedEvent extends ProductEvent {

    private String name;

    public ProductDeactivatedEvent() {
        super(EventType.PRODUCT_DEACTIVATED);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getRoutingKey() {
        return EventRoutingKey.PRODUCT_DEACTIVATED;
    }
}
