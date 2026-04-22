package com.ecommerce.userservice.filter;

import com.ecommerce.common.trace.TraceConstants;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;


public class TraceIdFilter implements Filter {


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {

            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

            String traceId = httpRequest.getHeader(TraceConstants.HEADER);

            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString();
            }

            MDC.put(TraceConstants.TRACE_ID, traceId);
            httpResponse.setHeader(TraceConstants.HEADER, traceId);

            filterChain.doFilter(servletRequest, servletResponse);

        } finally {
            MDC.clear();
        }
    }
}