package com.ecommerce.orderservice.domain.entity;

import com.ecommerce.orderservice.domain.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "catalog_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCatalog {
    @Id
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private Integer availableStock;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    @Column(nullable = false)
    private LocalDateTime lastSyncedAt;

}
