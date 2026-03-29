package com.ecommerce.userservice.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ProductDTO {
    private Long id;
    private String name;
}