package com.ecommerce.productservice.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.productservice.domain.entity.Category;
import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.enums.ProductErrorCode;
import com.ecommerce.productservice.domain.enums.ProductStatus;
import com.ecommerce.productservice.dto.request.ProductRequest;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.mapper.ProductMapper;
import com.ecommerce.productservice.repository.CategoryRepository;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductResponse createProduct(ProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(
                        "Category not found",
                        ProductErrorCode.CATEGORY_NOT_FOUND
                ));


        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(category)
                .status(ProductStatus.ACTIVE)
                .build();

        return productMapper.toProductResponse(productRepository.save(product));
    }

    public List<ProductResponse> getAllProducts() {

        return productRepository.findByStatus(ProductStatus.ACTIVE)
                .stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    public ProductResponse getById(Long id) {
        Product product = getProductById(id);
        return productMapper.toProductResponse(product);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = getProductById(id);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(
                        "Category not found",
                        ProductErrorCode.CATEGORY_NOT_FOUND
                ));


        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);

        return productMapper.toProductResponse(productRepository.save(product));
    }

    public void deactivateProduct(Long id) {

        Product product = getProductById(id);

        if (product.getStatus() == ProductStatus.INACTIVE) {
            throw new BusinessException(
                    "Product is already inactive",
                    ProductErrorCode.PRODUCT_INACTIVE
            );
        }

        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    public void activateProduct(Long id) {

        Product product = getProductById(id);

        if (product.getStatus() == ProductStatus.ACTIVE) {
            throw new BusinessException(
                    "Product is already active",
                    ProductErrorCode.PRODUCT_ALREADY_ACTIVE
            );
        }

        validateProductForActivation(product);

        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);
    }

    public ProductResponse updateStock(Long id, Integer stock) {

        if (stock < 0) {
            throw new BusinessException(
                    "Stock cannot be negative",
                    ProductErrorCode.INVALID_STOCK
            );
        }

        Product product = getProductById(id);
        validateProductIsActive(product);

        product.setStock(stock);

        return productMapper.toProductResponse(productRepository.save(product));
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
                    "Product is inactive",
                    ProductErrorCode.PRODUCT_INACTIVE
            );
        }
    }

}

