package com.ecommerce.productservice.converter;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.productservice.domain.enums.ProductErrorCode;
import com.ecommerce.productservice.domain.enums.ProductStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ProductStatusConverter
        implements Converter<String, ProductStatus> {

    @Override
    public ProductStatus convert(String source) {

        try {

            return ProductStatus.valueOf(source.toUpperCase());

        } catch (IllegalArgumentException ex) {

            throw new BusinessException(
                    "Invalid product status",
                    ProductErrorCode.INVALID_PRODUCT_STATUS
            );
        }
    }
}
