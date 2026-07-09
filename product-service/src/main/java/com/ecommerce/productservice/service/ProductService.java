package com.ecommerce.productservice.service;

import com.ecommerce.common.event.model.*;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.productservice.domain.entity.Category;
import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.enums.ProductErrorCode;
import com.ecommerce.productservice.domain.enums.ProductStatus;
import com.ecommerce.productservice.domain.specification.ProductSpecification;
import com.ecommerce.productservice.dto.request.ProductFilter;
import com.ecommerce.productservice.dto.request.ProductRequest;
import com.ecommerce.productservice.dto.request.ProductUpdateRequest;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.event.factory.ProductEventFactory;
import com.ecommerce.productservice.event.publisher.ProductEventPublisher;
import com.ecommerce.productservice.mapper.ProductMapper;
import com.ecommerce.productservice.repository.CategoryRepository;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductEventPublisher eventPublisher;
    private final ProductEventFactory  eventFactory;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductResponse createProduct(ProductRequest request) {

        Category category = findCategory(request.getCategoryId());

        Product product = buildProduct(request, category);

        Product productSaved = productRepository.save(product);

        ProductCreatedEvent event = eventFactory.buildProductCreatedEvent(productSaved);

        eventPublisher.publish(event);

        return productMapper.toProductResponse(productSaved);
    }

    public Page<ProductResponse> getAllProducts(ProductFilter filter, Pageable pageable) {
        boolean admin = isAdmin();

        Specification<Product> spec = ProductSpecification.withFilters(filter, admin);

        return productRepository.findAll(spec, pageable)
                .map(productMapper::toProductResponse);
    }

    public ProductResponse getById(Long id) {
        Product product = getProductById(id);

        if (isUser() && product.getStatus() == ProductStatus.INACTIVE) {
            throw new BusinessException(
                    "Product not available",
                    ProductErrorCode.PRODUCT_NOT_FOUND
            );
        }

        return productMapper.toProductResponse(product);
    }

    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {

        Product product = getProductById(id);

        Category category = findCategory(request.getCategoryId());


        updateProductFields(request, product, category);

        Product updatedProduct = productRepository.save(product);

        ProductUpdatedEvent event = eventFactory.buildProductUpdatedEvent(updatedProduct);

        eventPublisher.publish(event);

        return productMapper.toProductResponse(updatedProduct);
    }



    public void deactivateProduct(Long id) {

        Product product = getProductById(id);

        validateProductIsActive(product);

        product.setStatus(ProductStatus.INACTIVE);

        Product updatedProduct = productRepository.save(product);

        ProductDeactivatedEvent event = eventFactory.buildProductDeactivatedEvent(updatedProduct);

        eventPublisher.publish(event);

    }

    public void activateProduct(Long id) {

        Product product = getProductById(id);

        validateProductIsInactive(product);

        validateProductForActivation(product);

        product.setStatus(ProductStatus.ACTIVE);

        Product updatedProduct = productRepository.save(product);

        ProductActivatedEvent event = eventFactory.buildProductActivatedEvent(updatedProduct);

        eventPublisher.publish(event);

    }

    public ProductResponse updateStock(Long id, Integer stock) {

        validateStock(stock);

        Product product = getProductById(id);

        validateProductIsActive(product);

        product.setStock(stock);

        Product  updatedProduct = productRepository.save(product);

        ProductStockUpdatedEvent event = eventFactory.buildProductStockUpdatedEvent(updatedProduct);

        eventPublisher.publish(event);

        return productMapper.toProductResponse(updatedProduct);
    }


    private void validateProductForActivation(Product product) {

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Invalid price",
                    ProductErrorCode.INVALID_PRICE
            );
        }

        if (product.getName() == null || product.getName().isBlank()) {
            throw new BusinessException(
                    "Invalid name",
                    ProductErrorCode.INVALID_PRODUCT_NAME
            );
        }
    }

    private Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Product not found",
                        ProductErrorCode.PRODUCT_NOT_FOUND
                ));
    }

    private void validateProductIsActive(Product product) {
        if (product.getStatus() == ProductStatus.INACTIVE) {
            throw new BusinessException(
                    "Product is already inactive",
                    ProductErrorCode.PRODUCT_INACTIVE
            );
        }
    }

    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    private boolean isUser() {
        return hasRole("ROLE_USER");
    }

    private Category findCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(
                        "Category not found",
                        ProductErrorCode.CATEGORY_NOT_FOUND
                ));
        return category;
    }

    private Product buildProduct(ProductRequest request, Category category) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(category)
                .status(ProductStatus.ACTIVE)
                .build();
        return product;
    }

    private void updateProductFields(ProductUpdateRequest request, Product product, Category category) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(category);
    }

    private void validateProductIsInactive(Product product) {
        if (product.getStatus() == ProductStatus.ACTIVE) {
            throw new BusinessException(
                    "Product is already active",
                    ProductErrorCode.PRODUCT_ALREADY_ACTIVE
            );
        }
    }

    private static void validateStock(Integer stock) {
        if (stock < 0) {
            throw new BusinessException(
                    "Stock cannot be negative",
                    ProductErrorCode.INVALID_STOCK
            );
        }
    }

}

