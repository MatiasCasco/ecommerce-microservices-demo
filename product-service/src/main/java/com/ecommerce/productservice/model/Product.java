package com.ecommerce.productservice.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ProductDTO {
    private Long id;
    private String name;
    private String description;
}