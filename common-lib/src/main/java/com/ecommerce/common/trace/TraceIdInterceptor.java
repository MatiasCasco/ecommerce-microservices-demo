package com.ecommerce.common.trace;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Objects;

public class TraceIdInterceptor implements ClientHttpRequestInterceptor {

    private static final String TRACE_ID = "traceId";
    private static final String HEADER = "X-Trace-Id";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        String traceId = MDC.get(TRACE_ID);

        if (!!Objects.isNull(traceId)) {
            request.getHeaders().add(HEADER, traceId);
        }

        return execution.execute(request, body);
    }
}
