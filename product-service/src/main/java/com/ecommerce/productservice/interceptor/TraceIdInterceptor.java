package com.ecommerce.productservice.interceptor;

import com.ecommerce.common.trace.TraceConstants;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Objects;

public class TraceIdInterceptor implements ClientHttpRequestInterceptor {


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        String traceId = MDC.get(TraceConstants.TRACE_ID);

        if (!Objects.isNull(traceId)) {
            request.getHeaders().add(TraceConstants.HEADER, traceId);
        }

        return execution.execute(request, body);
    }
}
