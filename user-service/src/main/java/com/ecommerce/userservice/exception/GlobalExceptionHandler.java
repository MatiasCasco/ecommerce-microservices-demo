package com.ecommerce.userservice.exception;

import com.ecommerce.common.error.ErrorCode;
import com.ecommerce.common.error.ErrorResponse;
import com.ecommerce.common.error.GlobalErrorCode;
import com.ecommerce.common.exception.BaseException;
import com.ecommerce.common.util.CommerceLog;
import com.ecommerce.userservice.domain.enums.UserErrorCode;
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

    //  1. BusinessException (tu dominio)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BaseException ex,
            HttpServletRequest request
    ) {
        LOGGER.warn("{}",
                CommerceLog.exception(
                        "AUTH",
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

    //  2. Validaciones (@Valid)
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
                .orElse("Error de validación");

        LOGGER.warn("{}",
                CommerceLog.warn(
                        "AUTH",
                        "VALIDATION_ERROR",
                        message,
                        request.getRequestURI()
                )
        );

        return buildResponse(
                UserErrorCode.INVALID_INPUT,
                message,
                request
        );
    }

    //  3. Errores de entrada (manuales)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        LOGGER.warn("{}",
                CommerceLog.warn(
                        "AUTH",
                        "ILLEGAL_ARGUMENT",
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );

        return buildResponse(
                UserErrorCode.INVALID_INPUT,
                ex.getMessage(),
                request
        );
    }

    //  4. Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest request
    ) {
        LOGGER.error("{}",
                CommerceLog.error(
                        "AUTH",
                        "UNEXPECTED_ERROR",
                        ex.getMessage(),
                        request.getRequestURI()
                ),
                ex // stacktrace
        );

        return buildResponse(
                GlobalErrorCode.INTERNAL_ERROR,
                "Ocurrió un error interno",
                request
        );
    }

    //  Método reutilizable (CLAVE)
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
