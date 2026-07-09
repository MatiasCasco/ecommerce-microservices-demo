package com.ecommerce.common.event.model;

import com.ecommerce.common.event.constants.EventRoutingKey;
import com.ecommerce.common.event.constants.EventType;

public class ProductActivatedEvent extends ProductEvent {

    private String name;

    public ProductActivatedEvent() {
        super(EventType.PRODUCT_ACTIVATED);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getRoutingKey() {
        return EventRoutingKey.PRODUCT_ACTIVATED;
    }
}
