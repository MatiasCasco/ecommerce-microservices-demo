package com.ecommerce.common.exception;

import com.ecommerce.common.error.GlobalErrorCode;

public class UnauthorizedException  extends BaseException {

    private static final String UNAUTHORIZED = "Unauthorized";

    public UnauthorizedException() {
        super(UNAUTHORIZED, GlobalErrorCode.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(message, GlobalErrorCode.UNAUTHORIZED);
    }
}
