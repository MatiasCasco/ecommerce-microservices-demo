package com.ecommerce.productservice.domain.enums;

import com.ecommerce.common.error.ErrorCode;

public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(404, "PRODUCT_NOT_FOUND"),
    CATEGORY_NOT_FOUND(404, "CATEGORY_NOT_FOUND"),
    PRODUCT_ALREADY_EXISTS(409, "PRODUCT_ALREADY_EXISTS"),
    CATEGORY_ALREADY_EXISTS(409, "CATEGORY_ALREADY_EXISTS"),

    INVALID_STOCK(400, "INVALID_STOCK"),
    INVALID_PRICE(400, "INVALID_PRICE"),
    INVALID_PRODUCT_NAME(409, "INVALID_PRODUCT_NAME"),

    PRODUCT_INACTIVE(403, "PRODUCT_INACTIVE"),
    PRODUCT_ALREADY_ACTIVE(409, "PRODUCT_ALREADY_ACTIVE"),
    INVALID_INPUT(400, "INVALID_INPUT"),
    INVALID_PRODUCT_STATUS(400, "INVALID_PRODUCT_STATUS");

    private final int status;
    private final String code;

    ProductErrorCode(int status, String code) {
        this.status = status;
        this.code = code;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }
}
