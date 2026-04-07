package com.ecommerce.common.error;

import lombok.Getter;
import org.slf4j.MDC;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private int status;
    private String code;
    private String message;
    private String path;
    private String traceId;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String code, String message, String path) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.traceId = MDC.get("traceId");
        this.timestamp = LocalDateTime.now();
    }

}
