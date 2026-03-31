package com.ecommerce.userservice.config;

import com.ecommerce.common.trace.TraceIdInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setInterceptors(List.of(new TraceIdInterceptor()));

        return restTemplate;
    }

}
