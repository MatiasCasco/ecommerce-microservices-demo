package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Long> {
    List<Product> findByStatus(ProductStatus status);
}
