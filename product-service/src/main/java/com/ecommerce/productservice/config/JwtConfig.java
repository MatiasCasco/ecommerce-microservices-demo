package com.ecommerce.productservice.config;

import com.ecommerce.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtSecret, expiration);
    }

}
