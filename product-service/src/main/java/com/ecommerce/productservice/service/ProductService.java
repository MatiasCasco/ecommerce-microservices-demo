package com.ecommerce.productservice.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.productservice.domain.entity.Category;
import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.enums.ProductErrorCode;
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

        if (request.getStock() < 0) {
            throw new BusinessException("Stock cannot be negative", ProductErrorCode.INVALID_STOCK);
        }

        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "Price must be greater than zero",
                    ProductErrorCode.INVALID_PRICE
            );
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(category)
                .build();

        return productMapper.toProductResponse(productRepository.save(product));
    }

    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Product not found",
                        ProductErrorCode.PRODUCT_NOT_FOUND
                ));

        return productMapper.toProductResponse(product);
    }
}

