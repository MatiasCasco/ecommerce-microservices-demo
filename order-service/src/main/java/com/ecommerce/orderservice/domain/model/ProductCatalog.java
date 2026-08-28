package com.ecommerce.orderservice.domain.model;

import com.ecommerce.orderservice.domain.enums.ProductStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCatalog {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer availableStock;
    private ProductStatus status;
    private LocalDateTime lastSyncedAt;
}
