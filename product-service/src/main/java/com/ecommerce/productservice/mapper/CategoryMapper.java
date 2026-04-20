package com.ecommerce.productservice.mapper;

import com.ecommerce.productservice.domain.entity.Category;
import com.ecommerce.productservice.dto.request.CategoryRequest;
import com.ecommerce.productservice.dto.response.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .build();
    }

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}