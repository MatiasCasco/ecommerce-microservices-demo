package com.ecommerce.orderservice.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ProductDTO {
    private Long id;
    private String name;
}