package com.ecommerce.productservice.dto.request;

import com.ecommerce.productservice.domain.enums.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductFilter {
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private ProductStatus status;
}
