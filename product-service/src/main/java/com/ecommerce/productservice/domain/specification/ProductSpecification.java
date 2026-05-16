package com.ecommerce.productservice.domain.specification;

import com.ecommerce.productservice.domain.entity.Product;
import com.ecommerce.productservice.domain.enums.ProductStatus;
import com.ecommerce.productservice.dto.request.ProductFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> withFilters(ProductFilter filter, boolean isAdmin) {
        return (root, query, cb) -> {

            Predicate predicates = cb.conjunction();

            if (filter.getName() != null) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("name")),
                                "%" + filter.getName().toLowerCase() + "%"));
            }

            if (filter.getMinPrice() != null) {
                predicates = cb.and(predicates,
                        cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates = cb.and(predicates,
                        cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }


            if (isAdmin) {
                if (filter.getStatus() != null) {
                    predicates = cb.and(predicates,
                            cb.equal(root.get("status"), filter.getStatus()));
                }
            } else {
                // USER → solo ACTIVE
                predicates = cb.and(predicates,
                        cb.equal(root.get("status"), ProductStatus.ACTIVE));
            }

            return predicates;
        };
    }

}
