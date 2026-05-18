package com.ecommerce.productservice.event.model;

public class ProductDeactivatedEvent extends ProductEvent {

    private String name;

    @Override
    public String getEventType() {
        return "PRODUCT_DEACTIVATED";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
