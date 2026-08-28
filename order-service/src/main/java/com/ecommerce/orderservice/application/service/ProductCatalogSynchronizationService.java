package com.ecommerce.orderservice.application.service;

import com.ecommerce.common.event.model.*;
import com.ecommerce.orderservice.application.port.in.ProductCatalogSynchronizationUseCase;
import com.ecommerce.orderservice.application.port.out.ProductCatalogRepository;
import com.ecommerce.orderservice.domain.enums.ProductStatus;
import com.ecommerce.orderservice.domain.model.ProductCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductCatalogSynchronizationService implements ProductCatalogSynchronizationUseCase {

    private final ProductCatalogRepository productCatalogRepository;

    private static final String PRODUCT_NOT_FOUND = "Product not found. id=";

    @Override
    public void createProduct(ProductCreatedEvent event) {
        ProductCatalog productCatalog = toDomain(event);
        productCatalogRepository.save(productCatalog);
    }



    @Override
    public void updateProduct(ProductUpdatedEvent event) {

        ProductCatalog product = findProduct(event.getAggregateId());
        updateDomainValues(event, product);
        productCatalogRepository.save(product);
    }

    @Override
    public void activateProduct(ProductActivatedEvent event) {

        ProductCatalog product = findProduct(event.getAggregateId());
        activationProcess(product);
        productCatalogRepository.save(product);
    }


    @Override
    public void deactivateProduct(ProductDeactivatedEvent event) {

        ProductCatalog product = findProduct(event.getAggregateId());
        inactivationProcess(product);
        productCatalogRepository.save(product);
    }



    @Override
    public void updateStock(ProductStockUpdatedEvent event) {

        ProductCatalog product = findProduct(event.getAggregateId());
        assignStockValue(event, product);
        productCatalogRepository.save(product);
    }

    private ProductCatalog findProduct(Long productId) {
        return productCatalogRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException(PRODUCT_NOT_FOUND + productId));

    }

    private static ProductCatalog toDomain(ProductCreatedEvent event) {
        return  ProductCatalog.builder()
                .id(event.getAggregateId())
                .name(event.getName())
                .price(event.getPrice())
                .availableStock(event.getAvailableStock())
                .status(ProductStatus.ACTIVE)
                .lastSyncedAt(LocalDateTime.now())
                .build();

    }

    private static void updateDomainValues(ProductUpdatedEvent event, ProductCatalog product) {
        product.setName(event.getName());
        product.setPrice(event.getPrice());
        product.setLastSyncedAt(LocalDateTime.now());
    }

    private static void activationProcess(ProductCatalog product) {
        product.setStatus(ProductStatus.ACTIVE);
        product.setLastSyncedAt(LocalDateTime.now());
    }

    private static void inactivationProcess(ProductCatalog product) {
        product.setStatus(ProductStatus.INACTIVE);
        product.setLastSyncedAt(LocalDateTime.now());
    }

    private static void assignStockValue(ProductStockUpdatedEvent event, ProductCatalog product) {
        product.setAvailableStock(event.getStock());
        product.setLastSyncedAt(LocalDateTime.now());
    }

}
