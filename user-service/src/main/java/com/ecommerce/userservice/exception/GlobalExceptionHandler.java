package com.ecommerce.userservice.exception;

import com.ecommerce.userservice.domain.enums.ErrorCode;
import com.ecommerce.userservice.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

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

        ErrorCode errorCode = ErrorCode.INVALID_INPUT;

        ErrorResponse error = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        ex.printStackTrace();

        ErrorCode errorCode = ErrorCode.INVALID_INPUT;

        ErrorResponse error = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();

        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

        ErrorResponse error = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(BusinessException ex, HttpServletRequest request) {
        ex.printStackTrace();

        ErrorCode errorCode = ex.getErrorCode();

        ErrorResponse error = new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                ex.getMessage(),
                request.getRequestURI()
        );


        return ResponseEntity.status(errorCode.getStatus()).body(error);
    }
}
