package com.ecommerce.userservice.domain.enums;

public enum ErrorCode {

    // 🔵 NEGOCIO
    USER_NOT_FOUND(404, "USER_NOT_FOUND"),
    EMAIL_ALREADY_EXISTS(400, "EMAIL_ALREADY_EXISTS"),
    USER_DISABLED(403, "USER_DISABLED"),

    // 🟡 VALIDACIÓN
    INVALID_INPUT(400, "INVALID_INPUT"),

    // 🔴 SISTEMA
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
