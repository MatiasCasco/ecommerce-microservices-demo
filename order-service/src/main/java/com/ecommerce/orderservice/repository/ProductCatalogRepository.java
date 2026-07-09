package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.domain.entity.ProductCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCatalogRepository extends JpaRepository<ProductCatalog, Long> {

}
