package com.ecommerce.productservice.event.model;

public class ProductStockUpdatedEvent extends ProductEvent {

    private Integer stock;

    @Override
    public String getEventType() {
        return "PRODUCT_STOCK_UPDATED";
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
