package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.domain.entity.Category;
import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.dto.request.ProductRequest;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ProductResponse create(@RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest);
    }

    @GetMapping
    public List<ProductResponse> findAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable Long id,
            @RequestBody ProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @PatchMapping("/{id}/stock")
    public ProductResponse updateStock(
            @PathVariable Long id,
            @RequestParam Integer stock
    ) {
        return productService.updateStock(id, stock);
    }
}
