package com.ecommerce.userservice.domain.enums;

public enum ErrorCode {

    USER_NOT_FOUND(404, "USER_NOT_FOUND"),
    INVALID_INPUT(400, "INVALID_INPUT"),
    INTERNAL_ERROR(500, "INTERNAL_ERROR");

    private final int status;
    private final String code;

    ErrorCode(int status, String code) {
        this.status = status;
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
