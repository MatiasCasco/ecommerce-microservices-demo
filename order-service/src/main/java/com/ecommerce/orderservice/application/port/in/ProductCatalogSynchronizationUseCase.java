package com.ecommerce.orderservice.application.port.in;

import com.ecommerce.common.event.model.*;

public interface ProductCatalogSynchronizationUseCase {

    void createProduct(ProductCreatedEvent event);

    void updateProduct(ProductUpdatedEvent event);

    void activateProduct(ProductActivatedEvent event);

    void deactivateProduct(ProductDeactivatedEvent event);

    void updateStock(ProductStockUpdatedEvent event);

}
