package com.ecommerce.userservice.domain.enums;

import com.ecommerce.common.error.ErrorCode;

public enum UserErrorCode implements ErrorCode {

    // 🔵 NEGOCIO
    USER_NOT_FOUND(404, "USER_NOT_FOUND"), /* 404 Not Found = el recurso no existe */
    EMAIL_ALREADY_EXISTS(409, "EMAIL_ALREADY_EXISTS"), /* 409 = CONFLICT */
    USER_DISABLED(403, "USER_DISABLED"), /* 403 = Forbidden (no permitido) */

    // 🟡 VALIDACIÓN
    INVALID_INPUT(400, "INVALID_INPUT");

    private final int status;
    private final String code;

    UserErrorCode(int status, String code) {
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
