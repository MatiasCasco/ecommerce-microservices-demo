package com.ecommerce.productservice.exception;

import com.ecommerce.common.error.ErrorCode;
import com.ecommerce.common.error.ErrorResponse;
import com.ecommerce.common.error.GlobalErrorCode;
import com.ecommerce.common.exception.BaseException;
import com.ecommerce.common.util.CommerceLog;
import com.ecommerce.productservice.domain.enums.ProductErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. BusinessException
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BaseException ex,
            HttpServletRequest request
    ) {
        LOGGER.warn("{}",
                CommerceLog.exception(
                        "PRODUCT",
                        ex,
                        request.getRequestURI(),
                        ex.getErrorCode()
                )
        );

        return buildResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                request
        );
    }

    // 2. Validaciones (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("Validation error");

        LOGGER.warn("{}",
                CommerceLog.warn(
                        "PRODUCT",
                        "VALIDATION_ERROR",
                        message,
                        request.getRequestURI()
                )
        );

        return buildResponse(
                ProductErrorCode.INVALID_INPUT,
                message,
                request
        );
    }

    // 3. IllegalArgument
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        LOGGER.warn("{}",
                CommerceLog.warn(
                        "PRODUCT",
                        "ILLEGAL_ARGUMENT",
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );

        return buildResponse(
                ProductErrorCode.INVALID_INPUT,
                ex.getMessage(),
                request
        );
    }

    // 4. Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest request
    ) {
        LOGGER.error("{}",
                CommerceLog.error(
                        "PRODUCT",
                        "UNEXPECTED_ERROR",
                        ex.getMessage(),
                        request.getRequestURI()
                ),
                ex
        );

        return buildResponse(
                GlobalErrorCode.INTERNAL_ERROR,
                "Unexpected internal error",
                request
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode errorCode,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(error);
    }
}
