package com.ecommerce.productservice.security;

import com.ecommerce.common.error.ErrorResponse;
import com.ecommerce.common.error.GlobalErrorCode;
import com.ecommerce.common.util.CommerceLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    private static final String MODULE = "PRODUCT";
    private static final String FORBIDDEN = "FORBIDDEN";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        LOGGER.warn("{}",
                CommerceLog.warn(
                        MODULE,
                        FORBIDDEN,
                        accessDeniedException.getMessage(),
                        request.getRequestURI()
                )
        );

        ErrorResponse error = new ErrorResponse(
                GlobalErrorCode.FORBIDDEN.getStatus(),
                GlobalErrorCode.FORBIDDEN.getCode(),
                FORBIDDEN,
                request.getRequestURI()
        );

        response.setStatus(GlobalErrorCode.FORBIDDEN.getStatus());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
