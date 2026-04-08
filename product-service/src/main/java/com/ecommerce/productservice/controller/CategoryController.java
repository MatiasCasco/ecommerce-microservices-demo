package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.domain.entity.Category;
import com.ecommerce.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @PostMapping
    public Category create(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    @GetMapping
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}
