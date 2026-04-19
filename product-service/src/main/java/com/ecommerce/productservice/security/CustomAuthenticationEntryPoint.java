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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    private static final String MODULE = "PRODUCT";
    private static final String UNAUTHORIZED = "UNAUTHORIZED";


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        LOGGER.warn("{}",
                CommerceLog.warn(
                        MODULE,
                        UNAUTHORIZED,
                        authException.getMessage(),
                        request.getRequestURI()
                )
        );

        ErrorResponse error = new ErrorResponse(
                GlobalErrorCode.UNAUTHORIZED.getStatus(),
                GlobalErrorCode.UNAUTHORIZED.getCode(),
                UNAUTHORIZED,
                request.getRequestURI()
        );

        response.setStatus(GlobalErrorCode.UNAUTHORIZED.getStatus());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
