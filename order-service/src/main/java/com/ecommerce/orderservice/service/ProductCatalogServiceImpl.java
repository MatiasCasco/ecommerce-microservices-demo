package com.ecommerce.orderservice.service;

import com.ecommerce.common.event.model.*;
import com.ecommerce.orderservice.domain.entity.ProductCatalog;
import com.ecommerce.orderservice.domain.enums.ProductStatus;
import com.ecommerce.orderservice.repository.ProductCatalogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductCatalogServiceImpl implements ProductCatalogService {

    private final ProductCatalogRepository productCatalogRepository;

    private static final String PRODUCT_NOT_FOUND = "Product not found. id=";

    @Override
    public void createProduct(ProductCreatedEvent event) {
        ProductCatalog productCatalog =  ProductCatalog.builder()
                .id(event.getAggregateId())
                .name(event.getName())
                .price(event.getPrice())
                .availableStock(event.getAvailableStock())
                .status(ProductStatus.ACTIVE)
                .lastSyncedAt(LocalDateTime.now())
                .build();

        productCatalogRepository.save(productCatalog);
    }

    @Override
    public void updateProduct(ProductUpdatedEvent event) {

        ProductCatalog product = findProduct(event.getAggregateId());

        product.setName(event.getName());
        product.setPrice(event.getPrice());
        product.setLastSyncedAt(LocalDateTime.now());

        productCatalogRepository.save(product);
    }

    @Override
    public void activateProduct(ProductActivatedEvent event) {
        ProductCatalog product = findProduct(event.getAggregateId());

        product.setStatus(ProductStatus.ACTIVE);
        product.setLastSyncedAt(LocalDateTime.now());

        productCatalogRepository.save(product);
    }

    @Override
    public void deactivateProduct(ProductDeactivatedEvent event) {
        ProductCatalog product = findProduct(event.getAggregateId());

        product.setStatus(ProductStatus.INACTIVE);
        product.setLastSyncedAt(LocalDateTime.now());

        productCatalogRepository.save(product);
    }

    @Override
    public void updateStock(ProductStockUpdatedEvent event) {
        ProductCatalog product = findProduct(event.getAggregateId());

        product.setAvailableStock(event.getStock());
        product.setLastSyncedAt(LocalDateTime.now());

        productCatalogRepository.save(product);
    }

    private ProductCatalog findProduct(Long productId) {
        return productCatalogRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(PRODUCT_NOT_FOUND + productId));

    }
}
