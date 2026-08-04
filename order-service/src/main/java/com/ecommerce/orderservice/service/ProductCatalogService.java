package com.ecommerce.orderservice.service;

import com.ecommerce.common.event.model.*;

public interface ProductCatalogService {

    void createProduct(ProductCreatedEvent event);

    void updateProduct(ProductUpdatedEvent event);

    void activateProduct(ProductActivatedEvent event);

    void deactivateProduct(ProductDeactivatedEvent event);

    void updateStock(ProductStockUpdatedEvent event);

}
