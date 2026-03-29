package com.ecommerce.common.error;

public enum GlobalErrorCode implements ErrorCode{

    INTERNAL_ERROR(500, "INTERNAL_ERROR"), /* 500 = error inesperado del servidor */
    UNAUTHORIZED(401, "UNAUTHORIZED"), /* 401 = UNAUTHORIZED (No estás autenticado) */
    FORBIDDEN(403, "FORBIDDEN");   /* 403 = Forbidden (no permitido, Sí sé quién sos, pero no podés hacer esto) */

    private final int status;
    private final String code;

    GlobalErrorCode(int status, String code) {
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
