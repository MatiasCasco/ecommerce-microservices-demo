package com.ecommerce.productservice.event.factory;

import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.event.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ProductEventFactory {

    public ProductCreatedEvent buildProductCreatedEvent(Product product) {

        ProductCreatedEvent event = new ProductCreatedEvent();

        event.setEventId(UUID.randomUUID());
        event.setOccurredAt(LocalDateTime.now());
        event.setProductId(product.getId());

        event.setName(product.getName());
        event.setDescription(product.getDescription());
        event.setPrice(product.getPrice());

        return event;
    }

    public ProductUpdatedEvent buildProductUpdatedEvent(Product product) {

        ProductUpdatedEvent event = new ProductUpdatedEvent();

        event.setEventId(UUID.randomUUID());
        event.setOccurredAt(LocalDateTime.now());
        event.setProductId(product.getId());

        event.setName(product.getName());
        event.setDescription(product.getDescription());
        event.setPrice(product.getPrice());

        return event;
    }

    public ProductDeactivatedEvent buildProductDeactivatedEvent(Product product) {

        ProductDeactivatedEvent event = new ProductDeactivatedEvent();

        event.setEventId(UUID.randomUUID());
        event.setOccurredAt(LocalDateTime.now());
        event.setProductId(product.getId());

        event.setName(product.getName());

        return event;

    }

    public ProductActivatedEvent buildProductActivatedEvent(Product product) {

        ProductActivatedEvent event = new ProductActivatedEvent();

        event.setEventId(UUID.randomUUID());
        event.setOccurredAt(LocalDateTime.now());
        event.setProductId(product.getId());

        event.setName(product.getName());

        return event;

    }

    public ProductStockUpdatedEvent buildProductStockUpdatedEvent(Product product) {

        ProductStockUpdatedEvent event = new ProductStockUpdatedEvent();

        event.setEventId(UUID.randomUUID());

        event.setOccurredAt(LocalDateTime.now());

        event.setProductId(product.getId());

        event.setStock(product.getStock());

        return event;
    }

}
