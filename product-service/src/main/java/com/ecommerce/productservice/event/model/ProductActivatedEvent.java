package com.ecommerce.productservice.event.model;

public class ProductActivatedEvent extends ProductEvent {

    private String name;

    @Override
    public String getEventType() {
        return "PRODUCT_ACTIVATED";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
