package com.ecommerce.productservice.security;

import com.ecommerce.common.error.ErrorResponse;
import com.ecommerce.common.error.GlobalErrorCode;
import com.ecommerce.common.logging.CommerceLog;
import com.ecommerce.common.trace.TraceConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    private static final String MODULE = "PRODUCT";
    private static final String UNAUTHORIZED = "UNAUTHORIZED";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        String traceId = MDC.get(TraceConstants.TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
            MDC.put(TraceConstants.TRACE_ID, traceId);
        }



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
