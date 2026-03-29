package com.ecommerce.common.exception;


import com.ecommerce.common.error.ErrorCode;

public class BusinessException extends BaseException {

    public BusinessException( String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode);
    }
}
